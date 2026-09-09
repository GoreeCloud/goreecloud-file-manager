package com.goreecloud.filemanager.storage

import com.goreecloud.filemanager.model.BrowserLocation
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.model.FileOperationOutcome
import com.goreecloud.filemanager.model.FileOperationResult
import com.goreecloud.filemanager.model.StorageProviderDescriptor
import java.io.InputStream
import java.io.OutputStream

interface FileStorageProvider {
    val descriptor: StorageProviderDescriptor
    val root: BrowserLocation

    fun list(directory: BrowserLocation): List<FileEntry>

    fun createFile(parent: BrowserLocation, name: String): FileOperationResult =
        FileOperationResult(
            outcome = FileOperationOutcome.REJECTED,
            message = "This storage provider does not support file creation.",
        )

    fun createFolder(parent: BrowserLocation, name: String): FileOperationResult =
        FileOperationResult(
            outcome = FileOperationOutcome.REJECTED,
            message = "This storage provider does not support folder creation.",
        )

    fun duplicate(entry: FileEntry, destination: BrowserLocation, newName: String): FileOperationResult =
        FileOperationResult(
            outcome = FileOperationOutcome.REJECTED,
            message = "This storage provider does not support duplication.",
        )

    fun openRead(entry: FileEntry): InputStream? = null

    fun openWrite(entry: FileEntry): OutputStream? = null

    fun rename(entry: FileEntry, newName: String): FileOperationResult =
        FileOperationResult(
            outcome = FileOperationOutcome.REJECTED,
            message = "This storage provider does not support rename.",
        )

    fun delete(entry: FileEntry): FileOperationResult =
        FileOperationResult(
            outcome = FileOperationOutcome.REJECTED,
            message = "This storage provider does not support delete.",
        )
}

object FileNamePolicy {
    const val MAX_NAME_LENGTH = 255

    fun normalize(input: String): String = input.trim()

    fun errorFor(input: String): String? {
        val name = normalize(input)
        return when {
            name.isEmpty() -> "Name cannot be empty."
            name == "." || name == ".." -> "Reserved path names are not allowed."
            '/' in name || '\\' in name -> "Names cannot contain path separators."
            '\u0000' in name -> "Names cannot contain a null character."
            name.length > MAX_NAME_LENGTH -> "Name is longer than $MAX_NAME_LENGTH characters."
            else -> null
        }
    }
}

fun FileEntry.asBrowserLocation(): BrowserLocation {
    require(type == FileItemType.FOLDER) { "Only folders can be opened as browser locations" }
    return BrowserLocation(
        providerId = providerId,
        resourceId = resourceId,
        displayName = displayName,
        capabilities = capabilities,
    )
}
