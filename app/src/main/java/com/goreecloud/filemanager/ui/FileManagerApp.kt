package com.goreecloud.filemanager.ui

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.goreecloud.filemanager.model.BrowserLocation
import com.goreecloud.filemanager.model.FileCapability
import com.goreecloud.filemanager.model.FileEntry
import com.goreecloud.filemanager.model.FileItemType
import com.goreecloud.filemanager.model.FileOperationResult
import com.goreecloud.filemanager.model.StorageAuthorizationKind
import com.goreecloud.filemanager.storage.FileNamePolicy
import com.goreecloud.filemanager.storage.FileStorageProvider
import com.goreecloud.filemanager.storage.LocalFileRepository
import com.goreecloud.filemanager.storage.SafTreeFileRepository
import com.goreecloud.filemanager.storage.asBrowserLocation

private enum class Destination { HOME, BROWSE }

@Composable
fun FileManagerApp(repository: LocalFileRepository) {
    GoreeCloudFileManagerTheme {
        val context = LocalContext.current
        val contentResolver = context.contentResolver
        val initialProviders = remember(repository, contentResolver) {
            buildList<FileStorageProvider> {
                add(repository)
                addAll(loadPersistedTreeProviders(contentResolver))
            }.distinctBy { it.descriptor.id }
        }

        var providers by remember { mutableStateOf(initialProviders) }
        var destination by remember { mutableStateOf(Destination.HOME) }
        var selectedProviderId by remember { mutableStateOf(repository.descriptor.id) }
        var locationStack by remember { mutableStateOf(listOf(repository.root)) }
        var refreshKey by remember { mutableStateOf(0) }
        var operationMessage by remember { mutableStateOf<String?>(null) }
        var showNewFolderDialog by remember { mutableStateOf(false) }
        var renameTarget by remember { mutableStateOf<FileEntry?>(null) }
        var deleteTarget by remember { mutableStateOf<FileEntry?>(null) }

        val selectedProvider = providers.firstOrNull { it.descriptor.id == selectedProviderId }
            ?: repository
        val currentLocation = locationStack.lastOrNull()
            ?.takeIf { it.providerId == selectedProvider.descriptor.id }
            ?: selectedProvider.root
        val listing = remember(selectedProviderId, currentLocation.resourceId, refreshKey) {
            runCatching { selectedProvider.list(currentLocation) }
        }
        val entries = listing.getOrDefault(emptyList())
        val listingError = listing.exceptionOrNull()?.let {
            "Unable to read this location. Android permission may have changed or the provider may be unavailable."
        }

        fun selectProvider(provider: FileStorageProvider) {
            selectedProviderId = provider.descriptor.id
            locationStack = listOf(provider.root)
            destination = Destination.BROWSE
            operationMessage = null
            refreshKey += 1
        }

        fun applyOperation(result: FileOperationResult) {
            operationMessage = result.message
            // Refresh even after an uncertain provider failure because a side effect may have occurred.
            refreshKey += 1
        }

        val treePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                if (!persistTreePermission(contentResolver, uri)) {
                    operationMessage = "Android did not grant persistent read access to that location."
                } else {
                    val provider = runCatching { SafTreeFileRepository(contentResolver, uri) }
                        .getOrNull()
                    if (provider == null) {
                        operationMessage = "The selected storage location could not be verified after authorization."
                    } else {
                        providers = (providers.filterNot { it.descriptor.id == provider.descriptor.id } + provider)
                            .distinctBy { it.descriptor.id }
                        selectedProviderId = provider.descriptor.id
                        locationStack = listOf(provider.root)
                        destination = Destination.BROWSE
                        operationMessage = "Added ${provider.descriptor.displayName}."
                        refreshKey += 1
                    }
                }
            }
        }

        if (showNewFolderDialog) {
            NameInputDialog(
                title = "New folder",
                initialValue = "",
                confirmLabel = "Create",
                onDismiss = { showNewFolderDialog = false },
                onConfirm = { name ->
                    showNewFolderDialog = false
                    applyOperation(selectedProvider.createFolder(currentLocation, name))
                },
            )
        }

        renameTarget?.let { target ->
            NameInputDialog(
                title = "Rename",
                initialValue = target.displayName,
                confirmLabel = "Rename",
                onDismiss = { renameTarget = null },
                onConfirm = { name ->
                    renameTarget = null
                    applyOperation(selectedProvider.rename(target, name))
                },
            )
        }

        deleteTarget?.let { target ->
            DeleteConfirmationDialog(
                target = target,
                onDismiss = { deleteTarget = null },
                onConfirm = {
                    deleteTarget = null
                    applyOperation(selectedProvider.delete(target))
                },
            )
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 720.dp
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    AppNavigationRail(destination) { destination = it }
                    AppScaffold(
                        destination = destination,
                        providers = providers,
                        selectedProvider = selectedProvider,
                        currentLocation = currentLocation,
                        entries = entries,
                        listingError = listingError,
                        operationMessage = operationMessage,
                        canNavigateUp = locationStack.size > 1,
                        onOpenProvider = ::selectProvider,
                        onAddStorage = { treePicker.launch(null) },
                        onOpenDirectory = { entry -> locationStack = locationStack + entry.asBrowserLocation() },
                        onNavigateUp = {
                            if (locationStack.size > 1) locationStack = locationStack.dropLast(1)
                        },
                        onRefresh = { refreshKey += 1 },
                        onCreateFolder = { showNewFolderDialog = true },
                        onRename = { renameTarget = it },
                        onDelete = { deleteTarget = it },
                        bottomNavigation = null,
                    )
                }
            } else {
                AppScaffold(
                    destination = destination,
                    providers = providers,
                    selectedProvider = selectedProvider,
                    currentLocation = currentLocation,
                    entries = entries,
                    listingError = listingError,
                    operationMessage = operationMessage,
                    canNavigateUp = locationStack.size > 1,
                    onOpenProvider = ::selectProvider,
                    onAddStorage = { treePicker.launch(null) },
                    onOpenDirectory = { entry -> locationStack = locationStack + entry.asBrowserLocation() },
                    onNavigateUp = {
                        if (locationStack.size > 1) locationStack = locationStack.dropLast(1)
                    },
                    onRefresh = { refreshKey += 1 },
                    onCreateFolder = { showNewFolderDialog = true },
                    onRename = { renameTarget = it },
                    onDelete = { deleteTarget = it },
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
    providers: List<FileStorageProvider>,
    selectedProvider: FileStorageProvider,
    currentLocation: BrowserLocation,
    entries: List<FileEntry>,
    listingError: String?,
    operationMessage: String?,
    canNavigateUp: Boolean,
    onOpenProvider: (FileStorageProvider) -> Unit,
    onAddStorage: () -> Unit,
    onOpenDirectory: (FileEntry) -> Unit,
    onNavigateUp: () -> Unit,
    onRefresh: () -> Unit,
    onCreateFolder: () -> Unit,
    onRename: (FileEntry) -> Unit,
    onDelete: (FileEntry) -> Unit,
    bottomNavigation: (@Composable () -> Unit)?,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (destination == Destination.HOME) "Files" else currentLocation.displayName)
                },
                navigationIcon = {
                    if (destination == Destination.BROWSE && canNavigateUp) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Go to parent folder")
                        }
                    }
                },
                actions = {
                    when (destination) {
                        Destination.HOME -> {
                            IconButton(onClick = onAddStorage) {
                                Icon(Icons.Outlined.Add, contentDescription = "Add storage location")
                            }
                        }
                        Destination.BROWSE -> {
                            if (FileCapability.CREATE_FOLDER in currentLocation.capabilities) {
                                IconButton(onClick = onCreateFolder) {
                                    Icon(Icons.Outlined.CreateNewFolder, contentDescription = "Create folder")
                                }
                            }
                            IconButton(onClick = onRefresh) {
                                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                            }
                        }
                    }
                },
            )
        },
        bottomBar = { bottomNavigation?.invoke() },
    ) { padding ->
        when (destination) {
            Destination.HOME -> HomeScreen(
                providers = providers,
                onOpenProvider = onOpenProvider,
                onAddStorage = onAddStorage,
                modifier = Modifier.padding(padding),
            )
            Destination.BROWSE -> BrowseScreen(
                provider = selectedProvider,
                entries = entries,
                listingError = listingError,
                operationMessage = operationMessage,
                onOpenDirectory = onOpenDirectory,
                onRename = onRename,
                onDelete = onDelete,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun HomeScreen(
    providers: List<FileStorageProvider>,
    onOpenProvider: (FileStorageProvider) -> Unit,
    onAddStorage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Your files", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Browse app storage or Android locations that you explicitly authorize. Platform protection state remains evidence-gated.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item { Text("Storage locations", style = MaterialTheme.typography.titleLarge) }
        items(providers, key = { it.descriptor.id }) { provider ->
            StorageLocationCard(provider = provider, onOpen = { onOpenProvider(provider) })
        }
        item {
            OutlinedButton(onClick = onAddStorage, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add storage location")
            }
        }
        item { StatusCard("GoreeCloud Drive", "Not connected in this development revision") }
        item { StatusCard("Sync", "Unknown — no authoritative runtime evidence connected") }
        item { StatusCard("Backup & Everkeep", "Unknown — recoverability is not inferred") }
        item { StatusCard("Privacy Shield", "Unknown — runtime privacy evidence not connected") }
        item { StatusCard("Wardveil Security", "Unknown — missing coverage is not reported as clean") }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun StorageLocationCard(provider: FileStorageProvider, onOpen: () -> Unit) {
    val scope = when (provider.descriptor.authorizationKind) {
        StorageAuthorizationKind.APP_PRIVATE -> "App-private Android storage"
        StorageAuthorizationKind.USER_SELECTED_PERSISTED -> "User-authorized Android document tree"
    }
    val access = if (provider.descriptor.isReadOnly) "Read only" else "Read/write when provider capabilities allow"

    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Storage, contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(provider.descriptor.displayName, style = MaterialTheme.typography.titleMedium)
                Text(scope, style = MaterialTheme.typography.bodySmall)
                Text(access, style = MaterialTheme.typography.bodySmall)
            }
        }
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
    provider: FileStorageProvider,
    entries: List<FileEntry>,
    listingError: String?,
    operationMessage: String?,
    onOpenDirectory: (FileEntry) -> Unit,
    onRename: (FileEntry) -> Unit,
    onDelete: (FileEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
        item {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(provider.descriptor.displayName, style = MaterialTheme.typography.labelLarge)
                Text(
                    if (provider.descriptor.isReadOnly) "Read-only authorized location" else "Provider capabilities determine available operations",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            HorizontalDivider()
        }
        listingError?.let { error ->
            item { MessageCard(error) }
        }
        operationMessage?.let { message ->
            item { MessageCard(message) }
        }
        if (entries.isEmpty() && listingError == null) {
            item {
                Column(
                    modifier = Modifier.fillParentMaxHeight(0.65f).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Outlined.Storage, contentDescription = null)
                    Spacer(Modifier.height(12.dp))
                    Text("This folder is empty", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            items(entries, key = { it.resourceId }) { entry ->
                FileRow(entry, onOpenDirectory, onRename, onDelete)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun MessageCard(message: String) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(message, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FileRow(
    entry: FileEntry,
    onOpenDirectory: (FileEntry) -> Unit,
    onRename: (FileEntry) -> Unit,
    onDelete: (FileEntry) -> Unit,
) {
    val isFolder = entry.type == FileItemType.FOLDER
    val canRename = FileCapability.RENAME in entry.capabilities
    val canDelete = FileCapability.DELETE in entry.capabilities
    var menuExpanded by remember(entry.resourceId) { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isFolder) { onOpenDirectory(entry) }
            .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isFolder) Icons.Outlined.Folder else Icons.AutoMirrored.Outlined.InsertDriveFile,
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
        if (canRename || canDelete) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "File actions")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    if (canRename) {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onRename(entry)
                            },
                        )
                    }
                    if (canDelete) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDelete(entry)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NameInputDialog(
    title: String,
    initialValue: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    val validationError = FileNamePolicy.errorFor(value)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    label = { Text("Name") },
                )
                validationError?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = validationError == null,
                onClick = { onConfirm(FileNamePolicy.normalize(value)) },
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DeleteConfirmationDialog(
    target: FileEntry,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete ${target.displayName}?") },
        text = {
            Text(
                if (target.type == FileItemType.FOLDER) {
                    "Only empty folders can be deleted in this development slice. File Manager has not verified a backup or recovery path for this item."
                } else {
                    "This deletes the item through its storage provider. File Manager has not verified a backup or recovery path for this item."
                },
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
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

private fun loadPersistedTreeProviders(contentResolver: ContentResolver): List<FileStorageProvider> =
    contentResolver.persistedUriPermissions
        .filter { it.isReadPermission }
        .mapNotNull { permission ->
            runCatching { SafTreeFileRepository(contentResolver, permission.uri) }.getOrNull()
        }

private fun persistTreePermission(contentResolver: ContentResolver, uri: Uri): Boolean {
    val readFlag = Intent.FLAG_GRANT_READ_URI_PERMISSION
    val writeFlag = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    if (runCatching { contentResolver.takePersistableUriPermission(uri, readFlag or writeFlag) }.isSuccess) {
        return true
    }
    return runCatching { contentResolver.takePersistableUriPermission(uri, readFlag) }.isSuccess
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_048_576 -> "${bytes / 1_024} KB"
    bytes < 1_073_741_824 -> "${bytes / 1_048_576} MB"
    else -> "${bytes / 1_073_741_824} GB"
}
