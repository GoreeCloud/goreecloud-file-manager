package com.goreecloud.filemanager.storage

import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.model.FileLocationKind
import java.io.File
import java.time.Instant

class LocalFileRepository(rootDirectory: File) {
    val root: File = rootDirectory.canonicalFile

    fun list(directory: File): List<FileEntry> {
        val safeDirectory = requireWithinRoot(directory)
        return safeDirectory.listFiles()
            .orEmpty()
            .sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
            .map { file ->
                FileEntry(
                    providerId = PROVIDER_ID,
                    resourceId = file.canonicalPath,
                    displayName = file.name.ifBlank { file.canonicalPath },
                    absolutePath = file.canonicalPath,
                    type = if (file.isDirectory) FileItemType.FOLDER else FileItemType.FILE,
                    locationKind = FileLocationKind.APP_PRIVATE_LOCAL,
                    sizeBytes = if (file.isFile) file.length() else null,
                    modifiedAt = file.lastModified()
                        .takeIf { it > 0L }
                        ?.let(Instant::ofEpochMilli),
                )
            }
    }

    fun resolve(entry: FileEntry): File = requireWithinRoot(File(entry.absolutePath))

    fun parentOf(directory: File): File? {
        val safeDirectory = requireWithinRoot(directory)
        if (safeDirectory == root) return null
        return safeDirectory.parentFile?.let(::requireWithinRoot)
    }

    private fun requireWithinRoot(file: File): File {
        val canonical = file.canonicalFile
        val rootPath = root.path
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
