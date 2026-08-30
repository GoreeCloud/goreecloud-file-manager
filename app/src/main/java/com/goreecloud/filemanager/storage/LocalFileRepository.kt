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
import java.io.File
import java.time.Instant

class LocalFileRepository(rootDirectory: File) : FileStorageProvider {
    private val rootDirectory: File = rootDirectory.canonicalFile

    override val descriptor = StorageProviderDescriptor(
        id = PROVIDER_ID,
        displayName = "App storage",
        locationKind = FileLocationKind.APP_PRIVATE_LOCAL,
        authorizationKind = StorageAuthorizationKind.APP_PRIVATE,
        isReadOnly = false,
    )

    override val root = BrowserLocation(
        providerId = PROVIDER_ID,
        resourceId = rootDirectory.path,
        displayName = "App storage",
        capabilities = setOf(
            FileCapability.READ,
            FileCapability.LIST_CHILDREN,
            FileCapability.CREATE_FOLDER,
        ),
    )

    override fun list(directory: BrowserLocation): List<FileEntry> {
        requireProvider(directory.providerId)
        val safeDirectory = requireWithinRoot(File(directory.resourceId))
        require(safeDirectory.isDirectory) { "Requested resource is not a directory" }

        return safeDirectory.listFiles()
            .orEmpty()
            .sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
            .map(::entryFor)
    }

    override fun createFolder(parent: BrowserLocation, name: String): FileOperationResult {
        requireProvider(parent.providerId)
        FileNamePolicy.errorFor(name)?.let {
            return FileOperationResult(FileOperationOutcome.REJECTED, it)
        }
        if (FileCapability.CREATE_FOLDER !in parent.capabilities) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "Folder creation is not available in this location.",
            )
        }

        val safeParent = requireWithinRoot(File(parent.resourceId))
        val normalizedName = FileNamePolicy.normalize(name)
        val target = requireWithinRoot(File(safeParent, normalizedName))
        if (target.exists()) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "An item named $normalizedName already exists.",
            )
        }

        return if (target.mkdir()) {
            FileOperationResult(
                outcome = FileOperationOutcome.SUCCEEDED,
                message = "Created folder $normalizedName.",
                resultingEntry = entryFor(target),
            )
        } else {
            FileOperationResult(
                FileOperationOutcome.FAILED,
                "Android did not create the folder.",
            )
        }
    }

    override fun rename(entry: FileEntry, newName: String): FileOperationResult {
        requireProvider(entry.providerId)
        FileNamePolicy.errorFor(newName)?.let {
            return FileOperationResult(FileOperationOutcome.REJECTED, it)
        }
        if (FileCapability.RENAME !in entry.capabilities) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "Rename is not available for this item.",
            )
        }

        val source = requireWithinRoot(File(entry.resourceId))
        val parent = source.parentFile
            ?.let(::requireWithinRoot)
            ?: return FileOperationResult(FileOperationOutcome.REJECTED, "The item has no writable parent.")
        val normalizedName = FileNamePolicy.normalize(newName)
        val target = requireWithinRoot(File(parent, normalizedName))

        if (target.exists()) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "An item named $normalizedName already exists.",
            )
        }

        return if (source.renameTo(target)) {
            FileOperationResult(
                outcome = FileOperationOutcome.SUCCEEDED,
                message = "Renamed ${entry.displayName} to $normalizedName.",
                resultingEntry = entryFor(target),
            )
        } else {
            FileOperationResult(FileOperationOutcome.FAILED, "Android did not rename the item.")
        }
    }

    override fun delete(entry: FileEntry): FileOperationResult {
        requireProvider(entry.providerId)
        if (FileCapability.DELETE !in entry.capabilities) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "Delete is not available for this item.",
            )
        }

        val source = requireWithinRoot(File(entry.resourceId))
        if (source.isDirectory && source.listFiles().orEmpty().isNotEmpty()) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "Recursive folder deletion is not enabled. Empty the folder first.",
            )
        }

        return if (source.delete()) {
            FileOperationResult(
                FileOperationOutcome.SUCCEEDED,
                "Deleted ${entry.displayName}.",
            )
        } else {
            FileOperationResult(FileOperationOutcome.FAILED, "Android did not delete the item.")
        }
    }

    private fun entryFor(file: File): FileEntry {
        val safeFile = requireWithinRoot(file)
        val isDirectory = safeFile.isDirectory
        val capabilities = buildSet {
            add(FileCapability.READ)
            add(FileCapability.RENAME)
            add(FileCapability.DELETE)
            if (isDirectory) {
                add(FileCapability.LIST_CHILDREN)
                add(FileCapability.CREATE_FOLDER)
            }
        }

        return FileEntry(
            providerId = PROVIDER_ID,
            resourceId = safeFile.path,
            displayName = safeFile.name.ifBlank { safeFile.path },
            type = if (isDirectory) FileItemType.FOLDER else FileItemType.FILE,
            locationKind = FileLocationKind.APP_PRIVATE_LOCAL,
            sizeBytes = if (safeFile.isFile) safeFile.length() else null,
            modifiedAt = safeFile.lastModified()
                .takeIf { it > 0L }
                ?.let(Instant::ofEpochMilli),
            capabilities = capabilities,
        )
    }

    private fun requireProvider(providerId: String) {
        require(providerId == PROVIDER_ID) { "Resource belongs to a different provider" }
    }

    private fun requireWithinRoot(file: File): File {
        val canonical = file.canonicalFile
        val rootPath = rootDirectory.path
        val candidatePath = canonical.path
        require(candidatePath == rootPath || candidatePath.startsWith(rootPath + File.separator)) {
            "Path escapes the configured provider root"
        }
        return canonical
    }

    companion object {
        const val PROVIDER_ID = "android-app-private"
    }
}
