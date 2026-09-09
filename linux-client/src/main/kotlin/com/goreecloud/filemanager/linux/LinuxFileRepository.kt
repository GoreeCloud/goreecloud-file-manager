package com.goreecloud.filemanager.linux

import com.goreecloud.filemanager.model.BrowserLocation
import com.goreecloud.filemanager.model.FileCapability
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.model.FileLocationKind
import com.goreecloud.filemanager.model.FileOperationOutcome
import com.goreecloud.filemanager.model.FileOperationResult
import com.goreecloud.filemanager.model.StorageAuthorizationKind
import com.goreecloud.filemanager.model.StorageProviderDescriptor
import com.goreecloud.filemanager.storage.FileNamePolicy
import com.goreecloud.filemanager.storage.FileStorageProvider
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.FileStore
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest

/**
 * Bounded Linux local-filesystem provider for the development client.
 *
 * The provider is intentionally rooted at one explicitly selected directory. It never resolves
 * `..` outside that root, never follows symbolic links, and does not grant recursive destructive
 * behavior. Crossing into another mounted filesystem remains readable when the OS permits it, but
 * mutation capabilities are withheld until mount-boundary policy is implemented and accepted.
 */
class LinuxFileRepository(rootPath: Path) : FileStorageProvider {
    private val normalizedRoot = rootPath.toAbsolutePath().normalize()
    private val realRoot = validateRoot(normalizedRoot)
    private val rootFileStore = Files.getFileStore(realRoot)

    override val descriptor = StorageProviderDescriptor(
        id = "linux-local-${stableId(realRoot)}",
        displayName = "Linux Local Files",
        locationKind = FileLocationKind.LOCAL_DEVICE,
        authorizationKind = StorageAuthorizationKind.USER_SELECTED_PERSISTED,
        isReadOnly = !Files.isWritable(realRoot),
    )

    override val root = BrowserLocation(
        providerId = descriptor.id,
        resourceId = ROOT_RESOURCE_ID,
        displayName = displayNameFor(realRoot),
        capabilities = directoryCapabilities(realRoot, includeEntryMutation = false),
    )

    override fun list(directory: BrowserLocation): List<FileEntry> {
        val path = resolveDirectory(directory)
        return Files.list(path).use { stream ->
            stream.toList()
                .sortedWith(compareBy<Path>({ !Files.isDirectory(it, LinkOption.NOFOLLOW_LINKS) }, { it.fileName.toString().lowercase() }))
                .map(::entryForPath)
        }
    }

    override fun createFile(parent: BrowserLocation, name: String): FileOperationResult =
        mutate("The Linux file could not be created.") {
            val parentPath = writableDirectory(parent)
            val normalizedName = validatedName(name)
            val target = childTarget(parentPath, normalizedName)
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                return@mutate rejected("An item with that name already exists.")
            }
            Files.createFile(target)
            succeeded("Created $normalizedName.", entryForPath(target))
        }

    override fun createFolder(parent: BrowserLocation, name: String): FileOperationResult =
        mutate("The Linux folder could not be created.") {
            val parentPath = writableDirectory(parent)
            val normalizedName = validatedName(name)
            val target = childTarget(parentPath, normalizedName)
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                return@mutate rejected("An item with that name already exists.")
            }
            Files.createDirectory(target)
            succeeded("Created $normalizedName.", entryForPath(target))
        }

    override fun openRead(entry: FileEntry): InputStream? = runCatching {
        validateProvider(entry.providerId)
        val path = resolveExisting(entry.resourceId, allowLeafSymlink = false)
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(path)) {
            return null
        }
        Files.newInputStream(path, StandardOpenOption.READ)
    }.getOrNull()

    override fun openWrite(entry: FileEntry): OutputStream? = runCatching {
        validateProvider(entry.providerId)
        val path = resolveExisting(entry.resourceId, allowLeafSymlink = false)
        if (
            !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) ||
            !Files.isWritable(path) ||
            !sameFileStore(path)
        ) {
            return null
        }
        Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
    }.getOrNull()

    override fun rename(entry: FileEntry, newName: String): FileOperationResult =
        mutate("The Linux item could not be renamed.") {
            validateProvider(entry.providerId)
            val source = mutableEntryPath(entry)
            val normalizedName = validatedName(newName)
            val target = childTarget(source.parent, normalizedName)
            if (source == target) {
                return@mutate succeeded("The name is unchanged.", entryForPath(source))
            }
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                return@mutate rejected("An item with that name already exists.")
            }
            Files.move(source, target)
            succeeded("Renamed ${entry.displayName} to $normalizedName.", entryForPath(target))
        }

    override fun delete(entry: FileEntry): FileOperationResult =
        mutate("The Linux item could not be deleted.") {
            validateProvider(entry.providerId)
            val path = mutableEntryPath(entry)
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && !isDirectoryEmpty(path)) {
                return@mutate rejected(
                    "Recursive folder deletion is not enabled. Remove the folder contents through an accepted recovery-aware workflow first.",
                )
            }
            Files.delete(path)
            succeeded("Deleted ${entry.displayName}.")
        }

    private fun resolveDirectory(location: BrowserLocation): Path {
        validateProvider(location.providerId)
        val path = resolveExisting(location.resourceId, allowLeafSymlink = false)
        require(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) { "Resource is not a directory." }
        return path
    }

    private fun writableDirectory(location: BrowserLocation): Path {
        val path = resolveDirectory(location)
        require(Files.isWritable(path)) { "Directory is not writable." }
        require(sameFileStore(path)) { "Mutations across a mount boundary are not enabled." }
        return path
    }

    private fun mutableEntryPath(entry: FileEntry): Path {
        require(entry.resourceId != ROOT_RESOURCE_ID) { "The provider root cannot be mutated." }
        val path = resolveExisting(entry.resourceId, allowLeafSymlink = false)
        require(sameFileStore(path)) { "Mutations across a mount boundary are not enabled." }
        require(Files.isWritable(path.parent)) { "Parent directory is not writable." }
        return path
    }

    private fun resolveExisting(resourceId: String, allowLeafSymlink: Boolean): Path {
        val candidate = candidateFor(resourceId)
        require(Files.exists(candidate, LinkOption.NOFOLLOW_LINKS)) { "Resource does not exist." }
        require(!containsSymbolicLink(candidate, includeLeaf = !allowLeafSymlink)) {
            "Symbolic-link traversal is not enabled."
        }
        if (!allowLeafSymlink) {
            require(!Files.isSymbolicLink(candidate)) { "Symbolic-link traversal is not enabled." }
        }
        return candidate
    }

    private fun candidateFor(resourceId: String): Path {
        val relative = when (resourceId) {
            ROOT_RESOURCE_ID, "" -> Path.of("")
            else -> Path.of(resourceId)
        }
        require(!relative.isAbsolute) { "Resource ID must be provider-relative." }
        val candidate = realRoot.resolve(relative).normalize()
        require(candidate.startsWith(realRoot)) { "Resource escapes the selected Linux root." }
        return candidate
    }

    private fun childTarget(parent: Path, name: String): Path {
        val target = parent.resolve(name).normalize()
        require(target.parent == parent && target.startsWith(realRoot)) { "Target escapes the selected Linux root." }
        require(!Files.isSymbolicLink(parent)) { "Symbolic-link traversal is not enabled." }
        return target
    }

    private fun entryForPath(path: Path): FileEntry {
        val attributes = Files.readAttributes(
            path,
            BasicFileAttributes::class.java,
            LinkOption.NOFOLLOW_LINKS,
        )
        val isSymlink = attributes.isSymbolicLink
        val type = when {
            isSymlink -> FileItemType.SYMLINK
            attributes.isDirectory -> FileItemType.FOLDER
            else -> FileItemType.FILE
        }
        val capabilities = when (type) {
            FileItemType.SYMLINK -> emptySet()
            FileItemType.FOLDER -> directoryCapabilities(path, includeEntryMutation = true)
            FileItemType.FILE -> fileCapabilities(path)
        }
        return FileEntry(
            providerId = descriptor.id,
            resourceId = resourceIdFor(path),
            displayName = displayNameFor(path),
            type = type,
            locationKind = FileLocationKind.LOCAL_DEVICE,
            sizeBytes = if (attributes.isRegularFile) attributes.size() else null,
            modifiedAt = attributes.lastModifiedTime()?.toInstant(),
            capabilities = capabilities,
        )
    }

    private fun directoryCapabilities(path: Path, includeEntryMutation: Boolean): Set<FileCapability> = buildSet {
        add(FileCapability.READ)
        add(FileCapability.LIST_CHILDREN)
        if (!Files.isSymbolicLink(path) && sameFileStore(path) && Files.isWritable(path)) {
            add(FileCapability.CREATE_FILE)
            add(FileCapability.CREATE_FOLDER)
        }
        if (
            includeEntryMutation &&
            path != realRoot &&
            !Files.isSymbolicLink(path) &&
            sameFileStore(path) &&
            Files.isWritable(path.parent)
        ) {
            add(FileCapability.RENAME)
            add(FileCapability.DELETE)
        }
    }

    private fun fileCapabilities(path: Path): Set<FileCapability> = buildSet {
        if (Files.isReadable(path)) {
            add(FileCapability.READ)
            add(FileCapability.COPY)
        }
        if (sameFileStore(path) && Files.isWritable(path.parent)) {
            add(FileCapability.RENAME)
            add(FileCapability.DELETE)
            if (Files.isReadable(path)) {
                add(FileCapability.MOVE)
            }
        }
    }

    private fun resourceIdFor(path: Path): String {
        if (path == realRoot) return ROOT_RESOURCE_ID
        return realRoot.relativize(path).joinToString("/") { it.toString() }
    }

    private fun sameFileStore(path: Path): Boolean = runCatching {
        Files.getFileStore(path) == rootFileStore
    }.getOrDefault(false)

    private fun containsSymbolicLink(path: Path, includeLeaf: Boolean): Boolean {
        if (path == realRoot) return false
        val parts = realRoot.relativize(path).iterator().asSequence().toList()
        val checkedParts = if (includeLeaf) parts else parts.dropLast(1)
        var current = realRoot
        for (part in checkedParts) {
            current = current.resolve(part)
            if (Files.isSymbolicLink(current)) return true
        }
        return false
    }

    private fun isDirectoryEmpty(path: Path): Boolean =
        Files.newDirectoryStream(path).use { !it.iterator().hasNext() }

    private fun validatedName(name: String): String {
        FileNamePolicy.errorFor(name)?.let { throw IllegalArgumentException(it) }
        return FileNamePolicy.normalize(name)
    }

    private fun validateProvider(providerId: String) {
        require(providerId == descriptor.id) { "Resource belongs to another storage provider." }
    }

    private inline fun mutate(failureMessage: String, block: () -> FileOperationResult): FileOperationResult =
        try {
            block()
        } catch (error: IllegalArgumentException) {
            rejected(error.message ?: "The requested Linux filesystem operation is not allowed.")
        } catch (_: Exception) {
            FileOperationResult(FileOperationOutcome.FAILED, failureMessage)
        }

    private fun succeeded(message: String, entry: FileEntry? = null) =
        FileOperationResult(FileOperationOutcome.SUCCEEDED, message, entry)

    private fun rejected(message: String) =
        FileOperationResult(FileOperationOutcome.REJECTED, message)

    companion object {
        private const val ROOT_RESOURCE_ID = "."

        private fun validateRoot(path: Path): Path {
            require(Files.exists(path, LinkOption.NOFOLLOW_LINKS)) { "Selected Linux root does not exist." }
            require(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) { "Selected Linux root is not a directory." }
            require(!Files.isSymbolicLink(path)) { "A symbolic link cannot be used as the Linux provider root." }
            return path.toRealPath()
        }

        private fun stableId(path: Path): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(path.toString().toByteArray(Charsets.UTF_8))
            return digest.take(8).joinToString("") { "%02x".format(it) }
        }

        private fun displayNameFor(path: Path): String = path.fileName?.toString() ?: path.toString()
    }
}
