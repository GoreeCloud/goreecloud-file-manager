# GoreeCloud File Manager User Manual

## Current availability

GoreeCloud File Manager is in active Android development. The current build is a development APK and is **not Stable or production accepted**.

Current Android development identity:

- Application: `GoreeCloud File Manager Dev`
- Package: `com.goreecloud.filemanager.dev`
- Version: `0.1.0-dev`
- versionCode: `1`
- Minimum Android: API 26
- Target API: 36

The current application provides a native GoreeCloud-owned file-management shell, app-private browsing, and user-authorized Android document-tree browsing. It does not yet provide the complete File Manager product described in the project specification.

## Home

Home shows the storage locations currently available to File Manager and keeps GoreeCloud platform status separate from ordinary file access.

The built-in **App storage** location is the application's private Android files directory. It is isolated from ordinary user storage and is primarily a development/provider foundation.

User-authorized document-tree locations appear after you explicitly add them. File Manager also displays GoreeCloud Drive, Sync, Backup/Everkeep, Privacy Shield, and Wardveil Security status as unavailable or unknown when no authoritative runtime integration is connected. These status cards are not decorative protection claims.

## Adding an Android storage location

1. On Home, choose **Add storage location** or use the add action in the top bar.
2. Android opens the system document-tree picker.
3. Choose a folder or supported document-provider root that you want File Manager to access.
4. Android asks you to grant access to that selected tree.
5. File Manager requests persistent read access and, when Android/provider policy allows it, persistent write access.

File Manager does not request unrestricted filesystem access for this workflow. Authorization is limited to the Android document tree you selected. A selected tree may represent local storage, removable storage, or another Android DocumentsProvider; File Manager therefore describes it as a user-authorized document tree rather than assuming every selected provider is local disk storage.

Persisted locations are rediscovered from Android's persisted URI permissions when the application starts. If Android or the backing provider later revokes or invalidates access, File Manager reports the location as unreadable rather than pretending it remains available.

## Browsing

Choose a storage-location card on Home to open it in Browse.

Folders can be opened by tapping their rows. The back arrow moves to the previous folder in the current File Manager navigation stack. Refresh re-queries the selected provider.

Available actions are capability-driven. File Manager only exposes an action when the current provider/item reports the corresponding capability and File Manager has the required persisted permission.

## Creating folders

When the current folder supports creation, use the **Create folder** action in the Browse top bar.

File Manager validates the requested name before sending the operation to the provider. Empty names, `.` and `..`, path separators, null characters, and names longer than the current portability limit are rejected.

A successful message means File Manager received a successful provider result and refreshed the directory. Provider failures are surfaced instead of being silently converted into success.

## Renaming

When an item supports rename, open its overflow menu and choose **Rename**.

File Manager validates the new name and asks the exact selected storage provider to perform the rename. For the app-private provider, conflicting names are rejected before mutation. Android document providers remain authoritative for any additional provider-specific naming and conflict rules.

## Deleting files and folders

When an item supports delete, open its overflow menu and choose **Delete**. A confirmation dialog warns that File Manager has not yet verified a backup or recovery path for that item.

The current development slice deliberately refuses recursive folder deletion. A folder must be empty before File Manager will request deletion. This prevents an early development build from recursively deleting a directory tree through a provider without the later Trash, backup, Everkeep, and destructive-operation safeguards.

Deletion currently uses the selected provider's delete operation. File Manager does not yet claim a unified Trash workflow or verified recovery for these operations.

## What capability-driven means

A user-authorized Android document provider can advertise different abilities for different items. A location may be readable but not writable, or may allow rename without allowing creation. File Manager keeps those provider capabilities explicit rather than assuming all filesystems support the same operations.

The current capability model includes read, child listing, create file, create folder, rename, delete, copy, and move concepts. Only capabilities backed by the current provider implementation are exposed. Copy, move, and create-file workflows remain future implementation work in this development stage.

## GoreeCloud platform status

The following GoreeCloud systems remain required platform integrations but are not yet production-accepted in this File Manager build:

- **GoreeCloud Drive** — first-party cloud file/resource authority.
- **GoreeCloud Sync** — synchronization and conflict authority. Sync is not backup.
- **Everkeep / GoreeCloud Backup** — continuity, backup, recovery, and preservation authority.
- **Privacy Shield** — consent, purpose limitation, minimization, exposure, and data-use authority.
- **Wardveil Security** — authoritative file/content security and protection evidence.
- **GoreeCloud Identity** — account, ownership, device, session, and delegated access authority.
- **GoreeCloud Mesh** — bounded cross-service coordination and event delivery.

Unknown, unavailable, stale, or unverified platform evidence must remain visible as such. The application must not turn missing evidence into a positive privacy, security, synchronization, backup, or recovery claim.

## Current limitations

The current Android development build does not yet provide complete copy/move/duplicate workflows, file creation, multi-selection, universal search, previews, tags, collections, sharing, GoreeCloud Drive runtime access, GoreeCloud Sync runtime access, unified Trash, version history, Operations Center, verified backup/Everkeep recovery, Wardveil runtime scanning evidence, Privacy Shield runtime authorization, production Identity/Mesh integration, system-wide GoreeCloud file-picker registration, production signing, store distribution, or Stable qualification.

Glaze UI 2.0.0 is the current design-system target, but representative-device visual, accessibility, input, responsiveness, performance, and current-Stable conformance acceptance remain required.

## Reporting development problems

When reporting a problem, include the File Manager version, Android version, device model, storage-provider type, the operation attempted, expected result, and observed result. Do not include passwords, tokens, private file contents, encryption keys, or other reusable secrets in bug reports.

## Acceptance language

A successful CI run proves only the checks performed by the workflow for the exact source revision, such as repository validation, unit tests, Android lint, APK assembly, package/application-label verification, and artifact publication. It does not by itself establish production security, privacy, recovery, accessibility, representative-device compatibility, controlled signing, deployment, or Stable qualification.
