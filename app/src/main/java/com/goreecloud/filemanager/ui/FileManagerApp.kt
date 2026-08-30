package com.goreecloud.filemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.storage.LocalFileRepository
import java.io.File

private enum class Destination { HOME, BROWSE }

@Composable
fun FileManagerApp(repository: LocalFileRepository) {
    GoreeCloudFileManagerTheme {
        var destination by remember { mutableStateOf(Destination.HOME) }
        var currentDirectory by remember { mutableStateOf(repository.root) }
        var refreshKey by remember { mutableStateOf(0) }
        val entries = remember(currentDirectory, refreshKey) { repository.list(currentDirectory) }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 720.dp
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    AppNavigationRail(destination) { destination = it }
                    AppScaffold(
                        destination = destination,
                        repository = repository,
                        currentDirectory = currentDirectory,
                        entries = entries,
                        onOpenDirectory = { currentDirectory = repository.resolve(it) },
                        onNavigateUp = { repository.parentOf(currentDirectory)?.let { currentDirectory = it } },
                        onRefresh = { refreshKey += 1 },
                        bottomNavigation = null,
                    )
                }
            } else {
                AppScaffold(
                    destination = destination,
                    repository = repository,
                    currentDirectory = currentDirectory,
                    entries = entries,
                    onOpenDirectory = { currentDirectory = repository.resolve(it) },
                    onNavigateUp = { repository.parentOf(currentDirectory)?.let { currentDirectory = it } },
                    onRefresh = { refreshKey += 1 },
                    bottomNavigation = { AppNavigationBar(destination) { destination = it } },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    destination: Destination,
    repository: LocalFileRepository,
    currentDirectory: File,
    entries: List<FileEntry>,
    onOpenDirectory: (FileEntry) -> Unit,
    onNavigateUp: () -> Unit,
    onRefresh: () -> Unit,
    bottomNavigation: (@Composable () -> Unit)?,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (destination == Destination.HOME) "Files" else currentDirectory.name.ifBlank { "Browse" }) },
                navigationIcon = {
                    if (destination == Destination.BROWSE && currentDirectory != repository.root) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Go to parent folder")
                        }
                    }
                },
                actions = {
                    if (destination == Destination.BROWSE) {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
            )
        },
        bottomBar = { bottomNavigation?.invoke() },
    ) { padding ->
        when (destination) {
            Destination.HOME -> HomeScreen(entries.size, Modifier.padding(padding))
            Destination.BROWSE -> BrowseScreen(entries, onOpenDirectory, Modifier.padding(padding))
        }
    }
}

@Composable
private fun HomeScreen(localItemCount: Int, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Your files", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Native File Manager foundation. Platform integrations remain evidence-gated.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item { StatusCard("App storage", "$localItemCount item(s) in the current app-private root") }
        item { StatusCard("GoreeCloud Drive", "Not connected in this foundation revision") }
        item { StatusCard("Sync", "Unknown — no authoritative runtime evidence connected") }
        item { StatusCard("Backup & Everkeep", "Unknown — recoverability is not inferred") }
        item { StatusCard("Privacy Shield", "Unknown — runtime privacy evidence not connected") }
        item { StatusCard("Wardveil Security", "Unknown — missing coverage is not reported as clean") }
    }
}

@Composable
private fun StatusCard(title: String, detail: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BrowseScreen(
    entries: List<FileEntry>,
    onOpenDirectory: (FileEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Outlined.Storage, contentDescription = null)
            Spacer(Modifier.height(12.dp))
            Text("This app-private folder is empty", style = MaterialTheme.typography.titleMedium)
            Text("Broader storage providers are not enabled yet.", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    LazyColumn(modifier.fillMaxSize()) {
        items(entries, key = { it.resourceId }) { entry ->
            FileRow(entry, onOpenDirectory)
            HorizontalDivider()
        }
    }
}

@Composable
private fun FileRow(entry: FileEntry, onOpenDirectory: (FileEntry) -> Unit) {
    val isFolder = entry.type == FileItemType.FOLDER
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isFolder) { onOpenDirectory(entry) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isFolder) Icons.Outlined.Folder else Icons.Outlined.InsertDriveFile,
            contentDescription = null,
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                if (isFolder) "Folder" else formatBytes(entry.sizeBytes ?: 0L),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AppNavigationBar(selected: Destination, onSelect: (Destination) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == Destination.HOME,
            onClick = { onSelect(Destination.HOME) },
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            label = { Text("Home") },
        )
        NavigationBarItem(
            selected = selected == Destination.BROWSE,
            onClick = { onSelect(Destination.BROWSE) },
            icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
            label = { Text("Browse") },
        )
    }
}

@Composable
private fun AppNavigationRail(selected: Destination, onSelect: (Destination) -> Unit) {
    NavigationRail {
        Spacer(Modifier.height(12.dp))
        NavigationRailItem(
            selected = selected == Destination.HOME,
            onClick = { onSelect(Destination.HOME) },
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            label = { Text("Home") },
        )
        NavigationRailItem(
            selected = selected == Destination.BROWSE,
            onClick = { onSelect(Destination.BROWSE) },
            icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
            label = { Text("Browse") },
        )
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_048_576 -> "${bytes / 1_024} KB"
    bytes < 1_073_741_824 -> "${bytes / 1_048_576} MB"
    else -> "${bytes / 1_073_741_824} GB"
}
