package com.goreecloud.filemanager.linux

import com.goreecloud.filemanager.model.FileItemType
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.writeText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LinuxDesktopControllerTest {
    @Test
    fun discoveryAndHighlightDoNotConstructProvider() {
        val root = Files.createTempDirectory("goreecloud-linux-desktop-discovery")
        val candidate = candidate(root)
        var providerConstructions = 0
        val controller = LinuxDesktopController(
            discoverLocations = { listOf(candidate) },
            providerFactory = {
                providerConstructions += 1
                LinuxFileRepository(it)
            },
        )

        assertEquals(0, providerConstructions)
        assertNull(controller.snapshot().providerId)
        assertNull(controller.snapshot().currentLocation)
        assertTrue(controller.snapshot().candidates.single().requiresExplicitSelection)

        val highlighted = controller.highlightCandidate(candidate)

        assertEquals(0, providerConstructions)
        assertEquals(candidate.path, highlighted.highlightedCandidate?.path)
        assertNull(highlighted.providerId)
        assertNull(highlighted.currentLocation)
    }

    @Test
    fun explicitOpenConstructsProviderAndListsRoot() {
        val root = Files.createTempDirectory("goreecloud-linux-desktop-open")
        root.resolve("Documents").createDirectory()
        root.resolve("notes.txt").writeText("GoreeCloud")
        val candidate = candidate(root)
        var providerConstructions = 0
        val controller = LinuxDesktopController(
            discoverLocations = { listOf(candidate) },
            providerFactory = {
                providerConstructions += 1
                LinuxFileRepository(it)
            },
        )

        controller.highlightCandidate(candidate)
        val opened = controller.openHighlightedCandidate()

        assertEquals(1, providerConstructions)
        assertEquals(candidate.path, opened.authorizedCandidate?.path)
        assertNotNull(opened.providerId)
        assertNotNull(opened.currentLocation)
        assertEquals(setOf("Documents", "notes.txt"), opened.entries.map { it.displayName }.toSet())
        assertFalse(opened.canNavigateBack)
        assertTrue(opened.statusMessage.contains("read-only browsing"))
    }

    @Test
    fun folderNavigationStaysInsideAuthorizedProviderAndSupportsBack() {
        val root = Files.createTempDirectory("goreecloud-linux-desktop-navigation")
        val folder = root.resolve("Folder").createDirectory()
        folder.resolve("inside.txt").writeText("inside")
        val candidate = candidate(root)
        val controller = LinuxDesktopController(discoverLocations = { listOf(candidate) })

        controller.highlightCandidate(candidate)
        val rootState = controller.openHighlightedCandidate()
        val folderEntry = rootState.entries.single { it.type == FileItemType.FOLDER }
        val folderState = controller.openFolder(folderEntry)

        assertEquals(rootState.providerId, folderState.providerId)
        assertEquals("Folder", folderState.currentLocation?.displayName)
        assertEquals(listOf("inside.txt"), folderState.entries.map { it.displayName })
        assertTrue(folderState.canNavigateBack)

        val back = controller.navigateBack()
        assertEquals(rootState.currentLocation, back.currentLocation)
        assertFalse(back.canNavigateBack)
    }

    @Test
    fun failedExplicitOpenDoesNotLeaveProviderAuthorized() {
        val base = Files.createTempDirectory("goreecloud-linux-desktop-failure")
        val missing = base.resolve("missing")
        val candidate = candidate(missing)
        val controller = LinuxDesktopController(discoverLocations = { listOf(candidate) })

        controller.highlightCandidate(candidate)
        val failed = controller.openHighlightedCandidate()

        assertNull(failed.authorizedCandidate)
        assertNull(failed.providerId)
        assertNull(failed.currentLocation)
        assertTrue(failed.entries.isEmpty())
        assertNotNull(failed.errorMessage)
    }

    @Test
    fun returningToLocationsClosesDesktopProviderBoundary() {
        val root = Files.createTempDirectory("goreecloud-linux-desktop-close")
        val candidate = candidate(root)
        val controller = LinuxDesktopController(discoverLocations = { listOf(candidate) })

        controller.highlightCandidate(candidate)
        assertNotNull(controller.openHighlightedCandidate().providerId)

        val locations = controller.returnToLocations()

        assertNull(locations.authorizedCandidate)
        assertNull(locations.providerId)
        assertNull(locations.currentLocation)
        assertTrue(locations.entries.isEmpty())
        assertEquals(candidate.path, locations.highlightedCandidate?.path)
    }

    @Test
    fun disappearingLocationDuringRefreshClosesAuthorizedProvider() {
        val root = Files.createTempDirectory("goreecloud-linux-desktop-refresh-disappear")
        val openedCandidate = candidate(root)
        var discovered = listOf(openedCandidate)
        val controller = LinuxDesktopController(discoverLocations = { discovered })

        controller.highlightCandidate(openedCandidate)
        assertNotNull(controller.openHighlightedCandidate().providerId)
        discovered = emptyList()

        val refreshed = controller.refreshCandidates()

        assertNull(refreshed.authorizedCandidate)
        assertNull(refreshed.providerId)
        assertNull(refreshed.currentLocation)
        assertTrue(refreshed.entries.isEmpty())
        assertNotNull(refreshed.errorMessage)
        assertTrue(refreshed.statusMessage.contains("closed"))
    }

    @Test
    fun mountIdentityChangeAtSamePathRequiresExplicitReopen() {
        val root = Files.createTempDirectory("goreecloud-linux-desktop-refresh-remount")
        val original = candidate(
            path = root,
            kind = LinuxLocationCandidateKind.REMOVABLE_MEDIA_CANDIDATE,
            filesystemType = "ext4",
            filesystemSource = "/dev/sdb1",
        )
        var discovered = listOf(original)
        val controller = LinuxDesktopController(discoverLocations = { discovered })

        controller.highlightCandidate(original)
        assertNotNull(controller.openHighlightedCandidate().providerId)

        val replacement = original.copy(filesystemSource = "/dev/sdc1")
        discovered = listOf(replacement)
        val refreshed = controller.refreshCandidates()

        assertNull(refreshed.authorizedCandidate)
        assertNull(refreshed.providerId)
        assertNull(refreshed.currentLocation)
        assertEquals(replacement.path, refreshed.highlightedCandidate?.path)
        assertNotNull(refreshed.errorMessage)
        assertTrue(refreshed.errorMessage!!.contains("choose Open location again", ignoreCase = true))
    }

    @Test
    fun discoveryFailureDuringRefreshClosesAuthorizedProvider() {
        val root = Files.createTempDirectory("goreecloud-linux-desktop-refresh-failure")
        val openedCandidate = candidate(root)
        var failDiscovery = false
        val controller = LinuxDesktopController(
            discoverLocations = {
                if (failDiscovery) error("synthetic discovery failure")
                listOf(openedCandidate)
            },
        )

        controller.highlightCandidate(openedCandidate)
        assertNotNull(controller.openHighlightedCandidate().providerId)
        failDiscovery = true

        val refreshed = controller.refreshCandidates()

        assertNull(refreshed.authorizedCandidate)
        assertNull(refreshed.providerId)
        assertNull(refreshed.currentLocation)
        assertTrue(refreshed.candidates.isEmpty())
        assertNotNull(refreshed.errorMessage)
        assertTrue(refreshed.statusMessage.contains("closed"))
    }

    private fun candidate(
        path: Path,
        kind: LinuxLocationCandidateKind = LinuxLocationCandidateKind.XDG_USER_DIRECTORY,
        filesystemType: String? = null,
        filesystemSource: String? = null,
    ) = LinuxLocationCandidate(
        displayName = path.fileName.toString(),
        path = path,
        kind = kind,
        source = "test",
        filesystemType = filesystemType,
        filesystemSource = filesystemSource,
    )
}
