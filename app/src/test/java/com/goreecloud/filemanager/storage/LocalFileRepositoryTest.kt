package com.goreecloud.filemanager.storage

import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.model.FileOperationOutcome
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalFileRepositoryTest {
    @Test
    fun createRenameAndDeleteAreVerifiedWithinRoot() {
        withRepository { root, repository ->
            val create = repository.createFolder(repository.root, "Projects")
            assertEquals(FileOperationOutcome.SUCCEEDED, create.outcome)
            assertTrue(File(root, "Projects").isDirectory)

            val folder = repository.list(repository.root).single { it.displayName == "Projects" }
            assertEquals(FileItemType.FOLDER, folder.type)

            val rename = repository.rename(folder, "Work")
            assertEquals(FileOperationOutcome.SUCCEEDED, rename.outcome)
            assertFalse(File(root, "Projects").exists())
            assertTrue(File(root, "Work").isDirectory)

            val renamed = repository.list(repository.root).single { it.displayName == "Work" }
            val delete = repository.delete(renamed)
            assertEquals(FileOperationOutcome.SUCCEEDED, delete.outcome)
            assertFalse(File(root, "Work").exists())
        }
    }

    @Test
    fun nonEmptyFolderDeletionIsRejected() {
        withRepository { root, repository ->
            val folder = File(root, "Keep")
            assertTrue(folder.mkdir())
            File(folder, "data.txt").writeText("important")

            val entry = repository.list(repository.root).single { it.displayName == "Keep" }
            val result = repository.delete(entry)

            assertEquals(FileOperationOutcome.REJECTED, result.outcome)
            assertTrue(File(folder, "data.txt").isFile)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun listingCannotEscapeConfiguredRoot() {
        withRepository { _, repository ->
            repository.list(
                repository.root.copy(resourceId = File(repository.root.resourceId).parentFile!!.path),
            )
        }
    }

    @Test
    fun fileNamePolicyRejectsPathLikeNames() {
        assertEquals("Name cannot be empty.", FileNamePolicy.errorFor("   "))
        assertEquals("Reserved path names are not allowed.", FileNamePolicy.errorFor(".."))
        assertEquals("Names cannot contain path separators.", FileNamePolicy.errorFor("a/b"))
        assertEquals(null, FileNamePolicy.errorFor("Quarterly Report"))
    }

    private fun withRepository(block: (File, LocalFileRepository) -> Unit) {
        val root = Files.createTempDirectory("goreecloud-file-manager-test").toFile()
        try {
            block(root, LocalFileRepository(root))
        } finally {
            root.deleteRecursively()
        }
    }
}
