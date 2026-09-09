package com.goreecloud.filemanager.storage

import com.goreecloud.filemanager.model.BrowserLocation
import com.goreecloud.filemanager.model.FileCapability
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.model.FileLocationKind
import com.goreecloud.filemanager.model.FileOperationOutcome
import com.goreecloud.filemanager.model.FileOperationResult
import com.goreecloud.filemanager.model.StorageAuthorizationKind
import com.goreecloud.filemanager.model.StorageProviderDescriptor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileTransferServiceTest {
    @Test
    fun copyPreservesBytesAcrossProviders() {
        val source = MemoryProvider("source")
        val destination = MemoryProvider("destination")
        val sourceEntry = source.put("report.txt", "GoreeCloud".toByteArray())
        val service = FileTransferService(mapOf(source.descriptor.id to source, destination.descriptor.id to destination))

        val result = service.copy(sourceEntry, destination.root, "copy.txt")

        assertEquals(FileOperationOutcome.SUCCEEDED, result.outcome)
        assertArrayEquals("GoreeCloud".toByteArray(), destination.bytes("copy.txt"))
        assertTrue(source.exists("report.txt"))
    }

    @Test
    fun moveDeletesSourceOnlyAfterVerifiedCopy() {
        val source = MemoryProvider("source")
        val destination = MemoryProvider("destination")
        val sourceEntry = source.put("report.txt", "GoreeCloud".toByteArray())
        val service = FileTransferService(mapOf(source.descriptor.id to source, destination.descriptor.id to destination))

        val result = service.move(sourceEntry, destination.root, "moved.txt")

        assertEquals(FileOperationOutcome.SUCCEEDED, result.outcome)
        assertFalse(source.exists("report.txt"))
        assertArrayEquals("GoreeCloud".toByteArray(), destination.bytes("moved.txt"))
    }

    @Test
    fun moveRejectsSameSizeCorruptionBeforeDeletingSource() {
        val source = MemoryProvider("source")
        val destination = MemoryProvider("destination", corruptOnWrite = true)
        val sourceEntry = source.put("report.txt", "GoreeCloud".toByteArray())
        val service = FileTransferService(mapOf(source.descriptor.id to source, destination.descriptor.id to destination))

        val result = service.move(sourceEntry, destination.root, "moved.txt")

        assertEquals(FileOperationOutcome.FAILED, result.outcome)
        assertTrue(result.message.contains("SHA-256"))
        assertTrue(source.exists("report.txt"))
        assertFalse(destination.exists("moved.txt"))
    }

    @Test
    fun moveKeepsBothFilesWhenSourceDeletionFails() {
        val source = MemoryProvider("source", failDelete = true)
        val destination = MemoryProvider("destination")
        val sourceEntry = source.put("report.txt", "GoreeCloud".toByteArray())
        val service = FileTransferService(mapOf(source.descriptor.id to source, destination.descriptor.id to destination))

        val result = service.move(sourceEntry, destination.root, "moved.txt")

        assertEquals(FileOperationOutcome.FAILED, result.outcome)
        assertTrue(source.exists("report.txt"))
        assertArrayEquals("GoreeCloud".toByteArray(), destination.bytes("moved.txt"))
    }

    private class MemoryProvider(
        id: String,
        private val failDelete: Boolean = false,
        private val corruptOnWrite: Boolean = false,
    ) : FileStorageProvider {
        private val files = linkedMapOf<String, ByteArray>()

        override val descriptor = StorageProviderDescriptor(
            id = id,
            displayName = id,
            locationKind = FileLocationKind.EXTERNAL,
            authorizationKind = StorageAuthorizationKind.APP_PRIVATE,
            isReadOnly = false,
        )

        override val root = BrowserLocation(
            providerId = id,
            resourceId = "root",
            displayName = id,
            capabilities = setOf(
                FileCapability.READ,
                FileCapability.LIST_CHILDREN,
                FileCapability.CREATE_FILE,
            ),
        )

        fun put(name: String, data: ByteArray): FileEntry {
            files[name] = data.copyOf()
            return entry(name)
        }

        fun bytes(name: String): ByteArray = files.getValue(name)

        fun exists(name: String): Boolean = files.containsKey(name)

        override fun list(directory: BrowserLocation): List<FileEntry> {
            require(directory.providerId == descriptor.id)
            return files.keys.map(::entry)
        }

        override fun createFile(parent: BrowserLocation, name: String): FileOperationResult {
            require(parent.providerId == descriptor.id)
            if (files.containsKey(name)) {
                return FileOperationResult(FileOperationOutcome.REJECTED, "already exists")
            }
            files[name] = byteArrayOf()
            return FileOperationResult(FileOperationOutcome.SUCCEEDED, "created", entry(name))
        }

        override fun openRead(entry: FileEntry): InputStream? =
            files[entry.resourceId]?.let(::ByteArrayInputStream)

        override fun openWrite(entry: FileEntry): OutputStream? {
            if (!files.containsKey(entry.resourceId)) {
                return null
            }
            return object : ByteArrayOutputStream() {
                override fun close() {
                    super.close()
                    val written = toByteArray()
                    if (corruptOnWrite && written.isNotEmpty()) {
                        written[0] = (written[0].toInt() xor 0x01).toByte()
                    }
                    files[entry.resourceId] = written
                }
            }
        }

        override fun delete(entry: FileEntry): FileOperationResult {
            if (failDelete) {
                return FileOperationResult(FileOperationOutcome.FAILED, "delete blocked")
            }
            val removed = files.remove(entry.resourceId) != null
            return FileOperationResult(
                if (removed) FileOperationOutcome.SUCCEEDED else FileOperationOutcome.FAILED,
                if (removed) "deleted" else "missing",
            )
        }

        private fun entry(name: String): FileEntry = FileEntry(
            providerId = descriptor.id,
            resourceId = name,
            displayName = name,
            type = FileItemType.FILE,
            locationKind = FileLocationKind.EXTERNAL,
            sizeBytes = files[name]?.size?.toLong(),
            modifiedAt = null,
            capabilities = setOf(
                FileCapability.READ,
                FileCapability.COPY,
                FileCapability.MOVE,
                FileCapability.DELETE,
            ),
        )
    }
}
