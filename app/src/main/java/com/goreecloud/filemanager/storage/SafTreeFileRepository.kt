package com.goreecloud.filemanager.storage

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import com.goreecloud.filemanager.model.BrowserLocation
import com.goreecloud.filemanager.model.FileCapability
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.model.FileLocationKind
import com.goreecloud.filemanager.model.FileOperationOutcome
import com.goreecloud.filemanager.model.FileOperationResult
import com.goreecloud.filemanager.model.StorageAuthorizationKind
import com.goreecloud.filemanager.model.StorageProviderDescriptor
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

class SafTreeFileRepository(
    private val contentResolver: ContentResolver,
    val treeUri: Uri,
) : FileStorageProvider {
    private val treeDocumentId: String = DocumentsContract.getTreeDocumentId(treeUri)
    private val rootDocumentUri: Uri =
        DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId)
    private val hasWritePermission: Boolean = contentResolver.persistedUriPermissions
        .firstOrNull { it.uri == treeUri }
        ?.isWritePermission == true
    private val rootMetadata: DocumentMetadata = queryDocument(rootDocumentUri)
        ?: throw IllegalStateException("The selected Android document tree is no longer readable.")

    override val descriptor = StorageProviderDescriptor(
        id = PROVIDER_PREFIX + treeUri,
        displayName = rootMetadata.displayName.ifBlank { "Authorized storage" },
        locationKind = FileLocationKind.USER_AUTHORIZED_DOCUMENT_TREE,
        authorizationKind = StorageAuthorizationKind.USER_SELECTED_PERSISTED,
        isReadOnly = !hasWritePermission,
    )

    override val root = BrowserLocation(
        providerId = descriptor.id,
        resourceId = rootDocumentUri.toString(),
        displayName = descriptor.displayName,
        capabilities = capabilitiesFor(rootMetadata),
    )

    override fun list(directory: BrowserLocation): List<FileEntry> {
        requireProvider(directory.providerId)
        require(FileCapability.LIST_CHILDREN in directory.capabilities) {
            "Requested resource does not expose child listing"
        }

        val documentUri = checkedDocumentUri(directory.resourceId)
        val documentId = DocumentsContract.getDocumentId(documentUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(documentUri, documentId)
        val cursor = contentResolver.query(childrenUri, PROJECTION, null, null, null)
            ?: throw IllegalStateException("The Android document provider did not return a directory listing.")

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    val metadata = it.readMetadata()
                    val childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, metadata.documentId)
                    add(metadata.toFileEntry(childUri))
                }
            }.sortedWith(
                compareByDescending<FileEntry> { entry -> entry.type == FileItemType.FOLDER }
                    .thenBy { entry -> entry.displayName.lowercase() },
            )
        }
    }

    override fun createFile(parent: BrowserLocation, name: String): FileOperationResult {
        requireProvider(parent.providerId)
        FileNamePolicy.errorFor(name)?.let {
            return FileOperationResult(FileOperationOutcome.REJECTED, it)
        }
        if (FileCapability.CREATE_FILE !in parent.capabilities) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "File creation is not available in this authorized location.",
            )
        }

        return runOperation("File creation") {
            val parentUri = checkedDocumentUri(parent.resourceId)
            val normalizedName = FileNamePolicy.normalize(name)
            val createdUri = DocumentsContract.createDocument(
                contentResolver,
                parentUri,
                DEFAULT_FILE_MIME_TYPE,
                normalizedName,
            ) ?: return@runOperation FileOperationResult(
                FileOperationOutcome.FAILED,
                "The Android document provider did not create the file.",
            )
            val metadata = queryDocument(createdUri)
                ?: return@runOperation FileOperationResult(
                    FileOperationOutcome.FAILED,
                    "The file was created but its resulting state could not be verified.",
                )

            FileOperationResult(
                outcome = FileOperationOutcome.SUCCEEDED,
                message = "Created file ${metadata.displayName}.",
                resultingEntry = metadata.toFileEntry(createdUri),
            )
        }
    }

    override fun createFolder(parent: BrowserLocation, name: String): FileOperationResult {
        requireProvider(parent.providerId)
        FileNamePolicy.errorFor(name)?.let {
            return FileOperationResult(FileOperationOutcome.REJECTED, it)
        }
        if (FileCapability.CREATE_FOLDER !in parent.capabilities) {
            return FileOperationResult(
                FileOperationOutcome.REJECTED,
                "Folder creation is not available in this authorized location.",
            )
        }

        return runOperation("Folder creation") {
            val parentUri = checkedDocumentUri(parent.resourceId)
            val normalizedName = FileNamePolicy.normalize(name)
            val createdUri = DocumentsContract.createDocument(
                contentResolver,
                parentUri,
                DocumentsContract.Document.MIME_TYPE_DIR,
                normalizedName,
            ) ?: return@runOperation FileOperationResult(
                FileOperationOutcome.FAILED,
                "The Android document provider did not create the folder.",
            )
            val metadata = queryDocument(createdUri)
                ?: return@runOperation FileOperationResult(
                    FileOperationOutcome.FAILED,
                    "The folder was created but its resulting state could not be verified.",
                )

            FileOperationResult(
                outcome = FileOperationOutcome.SUCCEEDED,
                message = "Created folder ${metadata.displayName}.",
                resultingEntry = metadata.toFileEntry(createdUri),
            )
        }
    }

    override fun openRead(entry: FileEntry): InputStream? {
        requireProvider(entry.providerId)
        if (entry.type != FileItemType.FILE || FileCapability.READ !in entry.capabilities) {
            return null
        }
        return contentResolver.openInputStream(checkedDocumentUri(entry.resourceId))
    }

    override fun openWrite(entry: FileEntry): OutputStream? {
        requireProvider(entry.providerId)
        if (entry.type != FileItemType.FILE || !hasWritePermission) {
            return null
        }
        return contentResolver.openOutputStream(checkedDocumentUri(entry.resourceId), "w")
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

        return runOperation("Rename") {
            val sourceUri = checkedDocumentUri(entry.resourceId)
            val normalizedName = FileNamePolicy.normalize(newName)
            val renamedUri = DocumentsContract.renameDocument(
                contentResolver,
                sourceUri,
                normalizedName,
            ) ?: return@runOperation FileOperationResult(
                FileOperationOutcome.FAILED,
                "The Android document provider did not return a renamed item.",
            )
            val metadata = queryDocument(renamedUri)
                ?: return@runOperation FileOperationResult(
                    FileOperationOutcome.FAILED,
                    "The item may have been renamed, but the resulting state could not be verified.",
                )

            FileOperationResult(
                outcome = FileOperationOutcome.SUCCEEDED,
                message = "Renamed ${entry.displayName} to ${metadata.displayName}.",
                resultingEntry = metadata.toFileEntry(renamedUri),
            )
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

        if (entry.type == FileItemType.FOLDER) {
            val folder = entry.asBrowserLocation()
            val children = runCatching { list(folder) }
                .getOrElse {
                    return FileOperationResult(
                        FileOperationOutcome.FAILED,
                        "The folder could not be checked safely before deletion.",
                    )
                }
            if (children.isNotEmpty()) {
                return FileOperationResult(
                    FileOperationOutcome.REJECTED,
                    "Recursive folder deletion is not enabled. Empty the folder first.",
                )
            }
        }

        return runOperation("Delete") {
            val deleted = DocumentsContract.deleteDocument(
                contentResolver,
                checkedDocumentUri(entry.resourceId),
            )
            if (deleted) {
                FileOperationResult(
                    FileOperationOutcome.SUCCEEDED,
                    "Deleted ${entry.displayName}.",
                )
            } else {
                FileOperationResult(
                    FileOperationOutcome.FAILED,
                    "The Android document provider did not confirm deletion.",
                )
            }
        }
    }

    private fun DocumentMetadata.toFileEntry(documentUri: Uri): FileEntry = FileEntry(
        providerId = descriptor.id,
        resourceId = documentUri.toString(),
        displayName = displayName,
        type = type,
        locationKind = FileLocationKind.USER_AUTHORIZED_DOCUMENT_TREE,
        sizeBytes = sizeBytes,
        modifiedAt = modifiedAt,
        capabilities = capabilitiesFor(this),
    )

    private fun capabilitiesFor(metadata: DocumentMetadata): Set<FileCapability> = buildSet {
        add(FileCapability.READ)
        if (metadata.type == FileItemType.FOLDER) {
            add(FileCapability.LIST_CHILDREN)
            if (
                hasWritePermission &&
                metadata.flags and DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE != 0
            ) {
                add(FileCapability.CREATE_FILE)
                add(FileCapability.CREATE_FOLDER)
            }
        } else {
            add(FileCapability.COPY)
        }
        if (
            hasWritePermission &&
            metadata.flags and DocumentsContract.Document.FLAG_SUPPORTS_RENAME != 0
        ) {
            add(FileCapability.RENAME)
        }
        if (
            hasWritePermission &&
            metadata.flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0
        ) {
            add(FileCapability.DELETE)
            if (metadata.type == FileItemType.FILE) {
                add(FileCapability.MOVE)
            }
        }
    }

    private fun queryDocument(documentUri: Uri): DocumentMetadata? {
        val safeUri = checkedDocumentUri(documentUri.toString())
        return contentResolver.query(safeUri, PROJECTION, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) null else cursor.readMetadata()
        }
    }

    private fun Cursor.readMetadata(): DocumentMetadata {
        val documentId = getString(getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
        val displayName = getString(getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
            .orEmpty()
        val mimeType = getString(getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE))
        val flags = getInt(getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_FLAGS))
        val sizeIndex = getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
        val modifiedIndex = getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
        val sizeBytes = if (isNull(sizeIndex)) null else getLong(sizeIndex)
        val modifiedAt = if (isNull(modifiedIndex)) {
            null
        } else {
            getLong(modifiedIndex).takeIf { it > 0L }?.let(Instant::ofEpochMilli)
        }

        return DocumentMetadata(
            documentId = documentId,
            displayName = displayName.ifBlank { documentId },
            type = if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                FileItemType.FOLDER
            } else {
                FileItemType.FILE
            },
            sizeBytes = sizeBytes,
            modifiedAt = modifiedAt,
            flags = flags,
        )
    }

    private fun checkedDocumentUri(resourceId: String): Uri {
        val uri = Uri.parse(resourceId)
        require(uri.authority == treeUri.authority) { "Document authority does not match the selected tree" }
        require(DocumentsContract.getTreeDocumentId(uri) == treeDocumentId) {
            "Document escapes the selected Android document tree"
        }
        return uri
    }

    private fun requireProvider(providerId: String) {
        require(providerId == descriptor.id) { "Resource belongs to a different provider" }
    }

    private inline fun runOperation(
        actionName: String,
        block: () -> FileOperationResult,
    ): FileOperationResult = try {
        block()
    } catch (error: SecurityException) {
        FileOperationResult(
            FileOperationOutcome.FAILED,
            "$actionName failed because Android storage permission is no longer available.",
        )
    } catch (error: IllegalArgumentException) {
        FileOperationResult(
            FileOperationOutcome.FAILED,
            "$actionName failed because the selected document provider rejected the request.",
        )
    } catch (error: IllegalStateException) {
        FileOperationResult(
            FileOperationOutcome.FAILED,
            "$actionName failed because the resulting document state could not be verified.",
        )
    }

    private data class DocumentMetadata(
        val documentId: String,
        val displayName: String,
        val type: FileItemType,
        val sizeBytes: Long?,
        val modifiedAt: Instant?,
        val flags: Int,
    )

    companion object {
        private const val PROVIDER_PREFIX = "android-saf-tree:"
        private const val DEFAULT_FILE_MIME_TYPE = "application/octet-stream"
        private val PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
        )
    }
}
