package com.goreecloud.filemanager.linux

import com.goreecloud.filemanager.model.FileCapability
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.model.FileOperationOutcome
import com.goreecloud.filemanager.storage.FileTransferService
import com.goreecloud.filemanager.storage.asBrowserLocation
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinuxFileRepositoryTest {
    @Test
    fun listShowsSymlinkWithoutGrantingTraversalCapabilities() {
        val root = Files.createTempDirectory("goreecloud-linux-root-")
        val folder = Files.createDirectory(root.resolve("folder"))
        Files.writeString(folder.resolve("inside.txt"), "inside")
        Files.writeString(root.resolve("plain.txt"), "plain")
        Files.createSymbolicLink(root.resolve("folder-link"), folder)

        val repository = LinuxFileRepository(root)
        val entries = repository.list(repository.root)

        val link = entries.single { it.displayName == "folder-link" }
        assertEquals(FileItemType.SYMLINK, link.type)
        assertTrue(link.capabilities.isEmpty())
        assertTrue(entries.single { it.displayName == "folder" }.capabilities.contains(FileCapability.LIST_CHILDREN))
    }

    @Test
    fun listRejectsProviderRelativeTraversalOutsideSelectedRoot() {
        val root = Files.createTempDirectory("goreecloud-linux-root-")
        val repository = LinuxFileRepository(root)
        val escaped = repository.root.copy(resourceId = "../outside")

        val failure = runCatching { repository.list(escaped) }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertTrue(failure?.message.orEmpty().contains("escapes the selected Linux root"))
    }

    @Test
    fun symlinkedDirectoryCannotBeOpenedAsDirectory() {
        val parent = Files.createTempDirectory("goreecloud-linux-parent-")
        val root = Files.createDirectory(parent.resolve("root"))
        val outside = Files.createDirectory(parent.resolve("outside"))
        Files.writeString(outside.resolve("private.txt"), "outside")
        Files.createSymbolicLink(root.resolve("outside-link"), outside)

        val repository = LinuxFileRepository(root)
        val link = repository.list(repository.root).single { it.displayName == "outside-link" }
        val location = repository.root.copy(resourceId = link.resourceId, displayName = link.displayName)

        val failure = runCatching { repository.list(location) }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertTrue(failure?.message.orEmpty().contains("Symbolic-link traversal is not enabled"))
    }

    @Test
    fun createRenameAndDeleteRemainNonRecursive() {
        val root = Files.createTempDirectory("goreecloud-linux-root-")
        val repository = LinuxFileRepository(root)

        val createdFolder = repository.createFolder(repository.root, "work")
        assertEquals(FileOperationOutcome.SUCCEEDED, createdFolder.outcome)
        val folder = requireNotNull(createdFolder.resultingEntry)
        val folderLocation = folder.asBrowserLocation()

        val createdFile = repository.createFile(folderLocation, "note.txt")
        assertEquals(FileOperationOutcome.SUCCEEDED, createdFile.outcome)
        val file = requireNotNull(createdFile.resultingEntry)
        repository.openWrite(file)!!.use { it.write("hello".toByteArray()) }

        val refusedFolderDelete = repository.delete(folder)
        assertEquals(FileOperationOutcome.REJECTED, refusedFolderDelete.outcome)
        assertTrue(refusedFolderDelete.message.contains("Recursive folder deletion is not enabled"))

        val renamed = repository.rename(file, "renamed.txt")
        assertEquals(FileOperationOutcome.SUCCEEDED, renamed.outcome)
        val renamedEntry = requireNotNull(renamed.resultingEntry)
        assertEquals("renamed.txt", renamedEntry.displayName)

        assertEquals(FileOperationOutcome.SUCCEEDED, repository.delete(renamedEntry).outcome)
        val refreshedFolder = repository.list(repository.root).single { it.displayName == "work" }
        assertEquals(FileOperationOutcome.SUCCEEDED, repository.delete(refreshedFolder).outcome)
        assertFalse(Files.exists(root.resolve("work")))
    }

    @Test
    fun providerRootCannotBeRenamedOrDeleted() {
        val root = Files.createTempDirectory("goreecloud-linux-root-")
        val repository = LinuxFileRepository(root)
        val syntheticRootEntry = com.goreecloud.filemanager.model.FileEntry(
            providerId = repository.descriptor.id,
            resourceId = repository.root.resourceId,
            displayName = repository.root.displayName,
            type = FileItemType.FOLDER,
            locationKind = repository.descriptor.locationKind,
            sizeBytes = null,
            modifiedAt = null,
            capabilities = repository.root.capabilities,
        )

        assertEquals(FileOperationOutcome.REJECTED, repository.rename(syntheticRootEntry, "other").outcome)
        assertEquals(FileOperationOutcome.REJECTED, repository.delete(syntheticRootEntry).outcome)
    }

    @Test
    fun sharedTransferCopiesAndMovesBetweenLinuxProvidersWithIntegrityVerification() {
        val sourceRoot = Files.createTempDirectory("goreecloud-linux-source-")
        val destinationRoot = Files.createTempDirectory("goreecloud-linux-destination-")
        val sourcePath = sourceRoot.resolve("report.bin")
        val data = ByteArray(4096) { index -> (index % 251).toByte() }
        sourcePath.writeBytes(data)

        val source = LinuxFileRepository(sourceRoot)
        val destination = LinuxFileRepository(destinationRoot)
        val service = FileTransferService(
            mapOf(
                source.descriptor.id to source,
                destination.descriptor.id to destination,
            ),
        )
        val sourceEntry = source.list(source.root).single { it.displayName == "report.bin" }

        val copy = service.copy(sourceEntry, destination.root, "copy.bin")
        assertEquals(FileOperationOutcome.SUCCEEDED, copy.outcome)
        assertArrayEquals(data, destinationRoot.resolve("copy.bin").readBytes())
        assertTrue(Files.exists(sourcePath))

        val refreshedSource = source.list(source.root).single { it.displayName == "report.bin" }
        val move = service.move(refreshedSource, destination.root, "moved.bin")
        assertEquals(FileOperationOutcome.SUCCEEDED, move.outcome)
        assertArrayEquals(data, destinationRoot.resolve("moved.bin").readBytes())
        assertFalse(Files.exists(sourcePath))
    }
}
