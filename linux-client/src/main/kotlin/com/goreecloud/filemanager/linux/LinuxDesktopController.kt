package com.goreecloud.filemanager.linux

import com.goreecloud.filemanager.model.BrowserLocation
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.storage.FileStorageProvider
import com.goreecloud.filemanager.storage.asBrowserLocation
import java.nio.file.Path

data class LinuxDesktopState(
    val candidates: List<LinuxLocationCandidate> = emptyList(),
    val highlightedCandidate: LinuxLocationCandidate? = null,
    val authorizedCandidate: LinuxLocationCandidate? = null,
    val providerId: String? = null,
    val currentLocation: BrowserLocation? = null,
    val entries: List<FileEntry> = emptyList(),
    val canNavigateBack: Boolean = false,
    val statusMessage: String = "Choose a discovered location, then explicitly open it.",
    val errorMessage: String? = null,
)

/**
 * Presentation controller for the non-production Linux desktop development surface.
 *
 * Discovery and provider authorization remain separate by construction. Merely discovering or
 * highlighting a location never invokes [providerFactory]. A provider is created only after the
 * explicit open action. This controller deliberately exposes browsing/navigation only; mutation
 * primitives remain outside the desktop UI until their recovery and interaction requirements are
 * implemented and accepted.
 */
class LinuxDesktopController(
    private val discoverLocations: () -> List<LinuxLocationCandidate> = {
        LinuxLocationDiscovery().discover()
    },
    private val providerFactory: (Path) -> FileStorageProvider = { path ->
        LinuxFileRepository(path)
    },
) {
    private var provider: FileStorageProvider? = null
    private val history = mutableListOf<BrowserLocation>()
    private var state = LinuxDesktopState()

    init {
        refreshCandidates()
    }

    fun snapshot(): LinuxDesktopState = state

    fun refreshCandidates(): LinuxDesktopState {
        val discovered = runCatching { discoverLocations() }
            .getOrElse {
                if (state.authorizedCandidate != null) {
                    closeAuthorizedProvider(
                        candidates = emptyList(),
                        highlightedCandidate = null,
                        statusMessage = "The previously opened Linux provider was closed because location discovery could not be refreshed.",
                        errorMessage = "Linux locations could not be rediscovered. Select and explicitly open a location again after discovery recovers.",
                    )
                } else {
                    state = state.copy(
                        candidates = emptyList(),
                        highlightedCandidate = null,
                        errorMessage = "Linux locations could not be discovered.",
                    )
                }
                return state
            }
            .distinctBy { it.path.toAbsolutePath().normalize().toString() }

        val highlighted = state.highlightedCandidate?.let { previous ->
            discovered.firstOrNull { sameCandidate(it, previous) }
        }

        val authorized = state.authorizedCandidate
        if (authorized != null) {
            val refreshedAuthorized = discovered.firstOrNull { sameCandidate(it, authorized) }
            if (refreshedAuthorized == null || !sameAuthorizationIdentity(refreshedAuthorized, authorized)) {
                closeAuthorizedProvider(
                    candidates = discovered,
                    highlightedCandidate = highlighted,
                    statusMessage = "The previously opened Linux provider was closed because its discovered location identity changed or disappeared.",
                    errorMessage = "The opened Linux location is no longer the same discovered location. Review the current candidate and choose Open location again.",
                )
                return state
            }

            state = state.copy(
                candidates = discovered,
                highlightedCandidate = highlighted,
                authorizedCandidate = refreshedAuthorized,
                errorMessage = null,
            )
            return state
        }

        state = state.copy(
            candidates = discovered,
            highlightedCandidate = highlighted,
            errorMessage = null,
        )
        return state
    }

    fun highlightCandidate(candidate: LinuxLocationCandidate): LinuxDesktopState {
        val current = state.candidates.firstOrNull { sameCandidate(it, candidate) }
        if (current == null) {
            state = state.copy(
                errorMessage = "That Linux location candidate is no longer available.",
            )
            return state
        }

        state = state.copy(
            highlightedCandidate = current,
            statusMessage = "Selected ${current.displayName}. Choose Open location to create the bounded provider.",
            errorMessage = null,
        )
        return state
    }

    fun openHighlightedCandidate(): LinuxDesktopState {
        val candidate = state.highlightedCandidate
        if (candidate == null) {
            state = state.copy(errorMessage = "Select a Linux location candidate first.")
            return state
        }

        val opened = runCatching {
            val openedProvider = providerFactory(candidate.path)
            val root = openedProvider.root
            val entries = openedProvider.list(root)
            Triple(openedProvider, root, entries)
        }.getOrElse {
            provider = null
            history.clear()
            state = state.copy(
                authorizedCandidate = null,
                providerId = null,
                currentLocation = null,
                entries = emptyList(),
                canNavigateBack = false,
                errorMessage = "The explicitly selected Linux location could not be opened.",
                statusMessage = "No Linux provider is currently authorized in the desktop development surface.",
            )
            return state
        }

        provider = opened.first
        history.clear()
        history += opened.second
        state = state.copy(
            authorizedCandidate = candidate,
            providerId = opened.first.descriptor.id,
            currentLocation = opened.second,
            entries = opened.third,
            canNavigateBack = false,
            statusMessage = "Opened ${candidate.displayName}. This desktop slice exposes read-only browsing only.",
            errorMessage = null,
        )
        return state
    }

    fun openFolder(entry: FileEntry): LinuxDesktopState {
        val currentProvider = provider
        if (currentProvider == null) {
            state = state.copy(errorMessage = "Open a Linux location before browsing folders.")
            return state
        }
        if (entry.providerId != currentProvider.descriptor.id || entry.type != FileItemType.FOLDER) {
            state = state.copy(errorMessage = "Only folders from the currently authorized provider can be opened.")
            return state
        }

        val location = runCatching { entry.asBrowserLocation() }.getOrElse {
            state = state.copy(errorMessage = "That folder could not be resolved inside the selected provider.")
            return state
        }
        val entries = runCatching { currentProvider.list(location) }.getOrElse {
            state = state.copy(errorMessage = "That folder could not be listed.")
            return state
        }

        history += location
        state = state.copy(
            currentLocation = location,
            entries = entries,
            canNavigateBack = history.size > 1,
            statusMessage = "Browsing ${location.displayName} inside the explicitly selected provider.",
            errorMessage = null,
        )
        return state
    }

    fun navigateBack(): LinuxDesktopState {
        val currentProvider = provider ?: return state
        if (history.size <= 1) {
            state = state.copy(canNavigateBack = false)
            return state
        }

        history.removeLast()
        val previous = history.last()
        val entries = runCatching { currentProvider.list(previous) }.getOrElse {
            state = state.copy(errorMessage = "The previous folder could not be listed.")
            return state
        }

        state = state.copy(
            currentLocation = previous,
            entries = entries,
            canNavigateBack = history.size > 1,
            statusMessage = "Returned to ${previous.displayName}.",
            errorMessage = null,
        )
        return state
    }

    fun refreshCurrent(): LinuxDesktopState {
        val currentProvider = provider
        val currentLocation = state.currentLocation
        if (currentProvider == null || currentLocation == null) {
            return refreshCandidates()
        }

        val entries = runCatching { currentProvider.list(currentLocation) }.getOrElse {
            state = state.copy(errorMessage = "The current Linux folder could not be refreshed.")
            return state
        }
        state = state.copy(
            entries = entries,
            statusMessage = "Refreshed ${currentLocation.displayName}.",
            errorMessage = null,
        )
        return state
    }

    fun returnToLocations(): LinuxDesktopState {
        provider = null
        history.clear()
        state = state.copy(
            authorizedCandidate = null,
            providerId = null,
            currentLocation = null,
            entries = emptyList(),
            canNavigateBack = false,
            statusMessage = "Provider closed. Choose a discovered location, then explicitly open it.",
            errorMessage = null,
        )
        return state
    }

    private fun closeAuthorizedProvider(
        candidates: List<LinuxLocationCandidate>,
        highlightedCandidate: LinuxLocationCandidate?,
        statusMessage: String,
        errorMessage: String,
    ) {
        provider = null
        history.clear()
        state = state.copy(
            candidates = candidates,
            highlightedCandidate = highlightedCandidate,
            authorizedCandidate = null,
            providerId = null,
            currentLocation = null,
            entries = emptyList(),
            canNavigateBack = false,
            statusMessage = statusMessage,
            errorMessage = errorMessage,
        )
    }

    private fun sameAuthorizationIdentity(
        first: LinuxLocationCandidate,
        second: LinuxLocationCandidate,
    ): Boolean {
        if (!sameCandidate(first, second) || first.kind != second.kind) return false
        return when (first.kind) {
            LinuxLocationCandidateKind.MOUNTED_FILESYSTEM,
            LinuxLocationCandidateKind.REMOVABLE_MEDIA_CANDIDATE,
            -> first.filesystemType == second.filesystemType &&
                first.filesystemSource == second.filesystemSource
            LinuxLocationCandidateKind.HOME,
            LinuxLocationCandidateKind.XDG_USER_DIRECTORY,
            -> true
        }
    }

    private fun sameCandidate(first: LinuxLocationCandidate, second: LinuxLocationCandidate): Boolean =
        first.path.toAbsolutePath().normalize() == second.path.toAbsolutePath().normalize()
}
