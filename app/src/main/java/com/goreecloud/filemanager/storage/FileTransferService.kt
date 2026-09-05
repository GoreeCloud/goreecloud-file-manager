package com.goreecloud.filemanager.storage

import com.goreecloud.filemanager.model.BrowserLocation
import com.goreecloud.filemanager.model.FileCapability
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.model.FileOperationOutcome
import com.goreecloud.filemanager.model.FileOperationResult
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

class FileTransferService(
    private val providers: Map<String, FileStorageProvider>,
) {
    fun copy(
        source: FileEntry,
        destination: BrowserLocation,
        newName: String = source.displayName,
    ): FileOperationResult = transfer(source, destination, newName, deleteSource = false)

    fun move(
        source: FileEntry,
        destination: BrowserLocation,
        newName: String = source.displayName,
    ): FileOperationResult = transfer(source, destination, newName, deleteSource = true)

    private fun transfer(
        source: FileEntry,
        destination: BrowserLocation,
        newName: String,
        deleteSource: Boolean,
    ): FileOperationResult {
        FileNamePolicy.errorFor(newName)?.let {
            return FileOperationResult(FileOperationOutcome.REJECTED, it)
        }
        if (source.type != FileItemType.FILE) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "Recursive folder transfer is not enabled.",
            )
        }
        val requiredCapability = if (deleteSource) FileCapability.MOVE else FileCapability.COPY
        if (requiredCapability !in source.capabilities) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                if (deleteSource) "Move is not available for this item." else "Copy is not available for this item.",
            )
        }
        if (FileCapability.CREATE_FILE !in destination.capabilities) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "The destination does not allow file creation.",
            )
        }

        val sourceProvider = providers[source.providerId]
            ?: return unavailableProvider(source.providerId)
        val destinationProvider = providers[destination.providerId]
            ?: return unavailableProvider(destination.providerId)

        val create = destinationProvider.createFile(destination, newName)
        if (!create.succeeded) {
            return create
        }
        val createdEntry = create.resultingEntry
            ?: return FileOperationResult(
                FileOperationOutcome.FAILED,
                "The destination file was created but its identity could not be verified.",
            )

        val sourceDigest = runCatching {
            val input = sourceProvider.openRead(source)
                ?: error("source stream unavailable")
            val output = destinationProvider.openWrite(createdEntry)
                ?: error("destination stream unavailable")
            input.use { reader ->
                output.use { writer ->
                    copyWithSha256(reader, writer)
                }
            }
        }.getOrElse {
            destinationProvider.delete(createdEntry)
            return FileOperationResult(
                FileOperationOutcome.FAILED,
                "The file transfer did not complete; the partial destination was removed when possible.",
            )
        }

        val verifiedEntry = runCatching {
            destinationProvider.list(destination)
                .firstOrNull { it.resourceId == createdEntry.resourceId }
                ?: destinationProvider.list(destination)
                    .firstOrNull { it.displayName == FileNamePolicy.normalize(newName) }
        }.getOrNull()

        if (verifiedEntry == null || (source.sizeBytes != null && verifiedEntry.sizeBytes != source.sizeBytes)) {
            destinationProvider.delete(verifiedEntry ?: createdEntry)
            return FileOperationResult(
                FileOperationOutcome.FAILED,
                "The destination file could not be verified after transfer.",
            )
        }

        val destinationDigest = runCatching {
            val input = destinationProvider.openRead(verifiedEntry)
                ?: error("destination verification stream unavailable")
            input.use(::sha256)
        }.getOrNull()

        if (destinationDigest == null || !sourceDigest.contentEquals(destinationDigest)) {
            destinationProvider.delete(verifiedEntry)
            return FileOperationResult(
                FileOperationOutcome.FAILED,
                "The destination failed SHA-256 integrity verification and was removed when possible.",
            )
        }

        if (!deleteSource) {
            return FileOperationResult(
                FileOperationOutcome.SUCCEEDED,
                "Copied ${source.displayName} to ${verifiedEntry.displayName}.",
                verifiedEntry,
            )
        }

        val delete = sourceProvider.delete(source)
        if (!delete.succeeded) {
            return FileOperationResult(
                FileOperationOutcome.FAILED,
                "The destination copy is verified, but the original could not be removed. Both files were kept.",
                verifiedEntry,
            )
        }

        return FileOperationResult(
            FileOperationOutcome.SUCCEEDED,
            "Moved ${source.displayName} to ${verifiedEntry.displayName}.",
            verifiedEntry,
        )
    }

    private fun copyWithSha256(input: InputStream, output: OutputStream): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            if (read == 0) continue
            digest.update(buffer, 0, read)
            output.write(buffer, 0, read)
        }
        output.flush()
        return digest.digest()
    }

    private fun sha256(input: InputStream): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            if (read == 0) continue
            digest.update(buffer, 0, read)
        }
        return digest.digest()
    }

    private fun unavailableProvider(providerId: String) = FileOperationResult(
        FileOperationOutcome.FAILED,
        "Storage provider $providerId is not currently available.",
    )
}
