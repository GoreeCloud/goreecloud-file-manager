package com.goreecloud.filemanager.linux

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Non-production Linux desktop development surface.
 *
 * This is the first native desktop presentation slice for the Linux target. It intentionally exposes
 * discovery, explicit provider selection, and read-only folder browsing only. It is not Linux Stable
 * acceptance, supported packaging, or complete Glaze UI conformance evidence.
 */
fun main() = application {
    val controller = remember { LinuxDesktopController() }
    val windowState = rememberWindowState(size = DpSize(1280.dp, 820.dp))

    Window(
        onCloseRequest = ::exitApplication,
        title = "GoreeCloud File Manager — Linux Development",
        state = windowState,
    ) {
        LinuxDesktopDevelopmentApp(controller)
    }
}

@Composable
private fun LinuxDesktopDevelopmentApp(controller: LinuxDesktopController) {
    var state by remember { mutableStateOf(controller.snapshot()) }
    val dark = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = if (dark) goreeCloudDarkScheme() else goreeCloudLightScheme(),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val showInspector = maxWidth >= 1180.dp && state.currentLocation != null
                val sidebarWidth = if (maxWidth >= 980.dp) 284.dp else 232.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    LocationSidebar(
                        state = state,
                        width = sidebarWidth,
                        onHighlight = { state = controller.highlightCandidate(it) },
                        onOpen = { state = controller.openHighlightedCandidate() },
                        onRefresh = { state = controller.refreshCandidates() },
                    )

                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        DesktopToolbar(
                            state = state,
                            onBack = { state = controller.navigateBack() },
                            onRefresh = { state = controller.refreshCurrent() },
                            onLocations = { state = controller.returnToLocations() },
                        )

                        if (state.currentLocation == null) {
                            LocationWelcome(
                                state = state,
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                            )
                        } else {
                            FileBrowser(
                                state = state,
                                onOpenFolder = { state = controller.openFolder(it) },
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                            )
                        }

                        StatusBar(state)
                    }

                    if (showInspector) {
                        DesktopInspector(state = state, width = 300.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationSidebar(
    state: LinuxDesktopState,
    width: Dp,
    onHighlight: (LinuxLocationCandidate) -> Unit,
    onOpen: () -> Unit,
    onRefresh: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(width).fillMaxHeight(),
        tonalElevation = 5.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "FM",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column {
                    Text("GoreeCloud", style = MaterialTheme.typography.labelMedium)
                    Text("File Manager", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider()

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text("Locations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Discovery only until you explicitly open a location.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(
                    items = state.candidates,
                    key = { it.path.toAbsolutePath().normalize().toString() },
                ) { candidate ->
                    NavigationDrawerItem(
                        label = {
                            Column {
                                Text(candidate.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    candidateKindLabel(candidate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        selected = state.highlightedCandidate?.path?.toAbsolutePath()?.normalize() ==
                            candidate.path.toAbsolutePath().normalize(),
                        onClick = { onHighlight(candidate) },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    )
                }
            }

            HorizontalDivider()
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onOpen,
                    enabled = state.highlightedCandidate != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open location")
                }
                OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("Refresh locations")
                }
                Text(
                    "Linux Development · GLAZE UI V1.3 target · conformance pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DesktopToolbar(
    state: LinuxDesktopState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onLocations: () -> Unit,
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp).padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(onClick = onBack, enabled = state.canNavigateBack) {
                Text("Back")
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    state.currentLocation?.displayName ?: "Linux locations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    currentPathLabel(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            OutlinedButton(onClick = onRefresh) {
                Text("Refresh")
            }
            if (state.currentLocation != null) {
                Button(onClick = onLocations) {
                    Text("Locations")
                }
            }
        }
    }
}

@Composable
private fun LocationWelcome(state: LinuxDesktopState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 720.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Choose where to browse",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "File Manager discovers desktop location candidates without opening them. Select a candidate in the sidebar, review it, then choose Open location to create the bounded provider.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Discovery is not authorization", fontWeight = FontWeight.SemiBold)
                    Text(
                        state.highlightedCandidate?.let {
                            "Selected candidate: ${it.displayName}\n${it.path}"
                        } ?: "No location is selected. No Linux filesystem provider has been created.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "The current desktop milestone exposes read-only browsing. Mutation, safe eject, production packaging, and Linux Stable acceptance remain separate work.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun FileBrowser(
    state: LinuxDesktopState,
    onOpenFolder: (FileEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Name", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            Text("Type", modifier = Modifier.width(110.dp), style = MaterialTheme.typography.labelMedium)
            Text("Size", modifier = Modifier.width(100.dp), style = MaterialTheme.typography.labelMedium)
            Text("Modified", modifier = Modifier.width(170.dp), style = MaterialTheme.typography.labelMedium)
        }
        HorizontalDivider()

        if (state.entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("This folder is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = state.entries,
                    key = { "${it.providerId}:${it.resourceId}" },
                ) { entry ->
                    FileRow(entry = entry, onOpenFolder = onOpenFolder)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun FileRow(entry: FileEntry, onOpenFolder: (FileEntry) -> Unit) {
    val openable = entry.type == FileItemType.FOLDER
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (openable) Modifier.clickable { onOpenFolder(entry) } else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (openable) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (entry.type == FileItemType.SYMLINK) {
                Text(
                    "Symbolic link · traversal disabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(fileTypeLabel(entry), modifier = Modifier.width(110.dp), style = MaterialTheme.typography.bodySmall)
        Text(formatSize(entry.sizeBytes), modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodySmall)
        Text(formatModified(entry), modifier = Modifier.width(170.dp), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DesktopInspector(state: LinuxDesktopState, width: Dp) {
    Surface(
        modifier = Modifier.width(width).fillMaxHeight(),
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Context", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            InspectorField("Selected root", state.authorizedCandidate?.path?.toString() ?: "None")
            InspectorField("Provider", state.providerId ?: "None")
            InspectorField("Current folder", state.currentLocation?.displayName ?: "None")

            HorizontalDivider()

            Text("Development boundary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Read-only desktop browsing is exposed here. Existing provider mutation primitives are intentionally not surfaced in this UI slice.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Mount lifecycle, safe eject, full keyboard/accessibility acceptance, supported packaging, and Linux production/Stable status remain pending.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InspectorField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StatusBar(state: LinuxDesktopState) {
    val error = state.errorMessage
    Surface(
        color = if (error != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = error ?: state.statusMessage,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (error != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun currentPathLabel(state: LinuxDesktopState): String {
    val candidate = state.authorizedCandidate ?: return "No provider authorized"
    val location = state.currentLocation ?: return "Candidate selected; provider not open"
    if (location.resourceId == "." || location.resourceId.isBlank()) return candidate.path.toString()
    return candidate.path.resolve(location.resourceId).normalize().toString()
}

private fun candidateKindLabel(candidate: LinuxLocationCandidate): String = when (candidate.kind) {
    LinuxLocationCandidateKind.HOME -> "Home"
    LinuxLocationCandidateKind.XDG_USER_DIRECTORY -> "XDG user directory"
    LinuxLocationCandidateKind.MOUNTED_FILESYSTEM -> candidate.filesystemType?.let { "Mounted filesystem · $it" } ?: "Mounted filesystem"
    LinuxLocationCandidateKind.REMOVABLE_MEDIA_CANDIDATE -> "Removable-media candidate"
}

private fun fileTypeLabel(entry: FileEntry): String = when (entry.type) {
    FileItemType.FILE -> "File"
    FileItemType.FOLDER -> "Folder"
    FileItemType.SYMLINK -> "Symlink"
}

private fun formatSize(sizeBytes: Long?): String {
    if (sizeBytes == null) return "—"
    if (sizeBytes < 1024) return "$sizeBytes B"
    val kib = sizeBytes / 1024.0
    if (kib < 1024) return "%.1f KiB".format(kib)
    val mib = kib / 1024.0
    if (mib < 1024) return "%.1f MiB".format(mib)
    return "%.1f GiB".format(mib / 1024.0)
}

private val modifiedFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneId.systemDefault())

private fun formatModified(entry: FileEntry): String =
    entry.modifiedAt?.let(modifiedFormatter::format) ?: "—"

private fun goreeCloudLightScheme() = lightColorScheme(
    primary = Color(0xFF0D756D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEDE8),
    onPrimaryContainer = Color(0xFF062F2C),
    secondary = Color(0xFF4F46E5),
    surface = Color(0xFFF8FAFB),
    surfaceVariant = Color(0xFFEFF2F4),
)

private fun goreeCloudDarkScheme() = darkColorScheme(
    primary = Color(0xFF71D7CC),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF0B514B),
    onPrimaryContainer = Color(0xFFCDEDE8),
    secondary = Color(0xFFB8B5FF),
    surface = Color(0xFF111416),
    surfaceVariant = Color(0xFF252A2D),
)
