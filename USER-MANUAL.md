# GoreeCloud File Manager User Manual

## Current availability

GoreeCloud File Manager is required to support **Linux and Android** as first-class native platforms. The current native user-facing application is Android: it is a development APK and is **not Stable or production accepted**.

A bounded Linux local-filesystem provider, read-only location-candidate discovery layer, and non-production command-line development harness now exist in source. They are engineering/validation surfaces, not the accepted GoreeCloud File Manager desktop application. There is currently **no accepted Linux desktop UI, supported Linux package, production Linux runtime, or Linux Stable acceptance**.

Current Android development identity:

- Application: `GoreeCloud File Manager Dev`
- Package: `com.goreecloud.filemanager.dev`
- Version: `0.1.0-dev`
- versionCode: `1`
- Minimum Android: API 26
- Target API: 36

The Android application provides a native GoreeCloud-owned file-management shell, app-private browsing, and user-authorized Android document-tree browsing. It does not yet provide the complete File Manager product described in the project specification.

## Home

Home shows the storage locations currently available to the Android File Manager application and keeps GoreeCloud platform status separate from ordinary file access.

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

## Browsing on Android

Choose a storage-location card on Home to open it in Browse.

Folders can be opened by tapping their rows. The back arrow moves to the previous folder in the current File Manager navigation stack. Refresh re-queries the selected provider.

Available actions are capability-driven. File Manager only exposes an action when the current provider/item reports the corresponding capability and File Manager has the required persisted permission.

## Creating folders

When the current Android folder supports creation, use the **Create folder** action in the Browse top bar.

File Manager validates the requested name before sending the operation to the provider. Empty names, `.` and `..`, path separators, null characters, and names longer than the current portability limit are rejected.

A successful message means File Manager received a successful provider result and refreshed the directory. Provider failures are surfaced instead of being silently converted into success.

The Linux development provider also contains a bounded create-folder primitive for testing inside its selected root, but the current Linux command-line harness intentionally does not expose a user-facing create command.

## Renaming

When an Android item supports rename, open its overflow menu and choose **Rename**.

File Manager validates the new name and asks the exact selected storage provider to perform the rename. For the app-private provider, conflicting names are rejected before mutation. Android document providers remain authoritative for any additional provider-specific naming and conflict rules.

The Linux development provider contains a bounded rename primitive, but it is not currently exposed as a production desktop workflow.

## Deleting files and folders

When an Android item supports delete, open its overflow menu and choose **Delete**. A confirmation dialog warns that File Manager has not yet verified a backup or recovery path for that item.

The current development slice deliberately refuses recursive folder deletion. A folder must be empty before File Manager will request deletion. This prevents an early development build from recursively deleting a directory tree through a provider without the later Trash, backup, Everkeep, and destructive-operation safeguards.

The Linux development provider enforces the same empty-folder-only boundary and rejects mutation of its provider root. Its command-line harness remains read-only and does not expose deletion to a user.

Deletion currently uses the selected provider's delete operation. File Manager does not yet claim a unified Trash workflow or verified recovery for these operations.

## What capability-driven means

A storage provider can advertise different abilities for different resources. A location may be readable but not writable, or may allow rename without allowing creation. File Manager keeps those provider capabilities explicit rather than assuming all filesystems/providers support the same operations.

The current capability model includes read, child listing, create file, create folder, rename, delete, copy, and move concepts. Only capabilities backed by the current provider implementation are exposed.

The shared backend contains verified ordinary-file transfer primitives. Complete destination-selection copy/move UI, recursive transfer, multi-selection, and complete file-creation/duplicate workflows remain development work.

## Linux development harness

Linux is a required File Manager product platform. The current repository contains a development-only JVM harness for validating the initial Linux provider, location-discovery, and application boundaries before a native desktop UI and supported package exist.

The explicit-root mode accepts exactly one root directory and performs a read-only listing:

```bash
gradle :core:test :linux-client:test
gradle :linux-client:installDist
./linux-client/build/install/linux-client/bin/linux-client /an/explicit/root
```

The location-discovery mode performs a read-only report of Linux location candidates:

```bash
./linux-client/build/install/linux-client/bin/linux-client --locations
```

`--locations` may report Home, recognized XDG user directories, user-facing mounted filesystems, and paths under `/media` or `/run/media` as removable-media candidates. It does **not** create a `LinuxFileRepository`, authorize a filesystem root, grant read/write access, or make a discovered path safe to eject. Every reported candidate still requires explicit selection before File Manager creates a provider boundary around it.

XDG discovery reads `user-dirs.dirs`. It expands only literal `$HOME` / `${HOME}` values or accepts explicit absolute paths; it does not execute shell expressions. Candidate directories must exist and cannot themselves be symbolic links in the current discovery slice. Mount candidates are read from Linux mount metadata and are filtered to avoid ordinary pseudo/system-only mount surfaces.

These are developer workflows, not supported Linux installation procedures or accepted desktop location UX.

The current Linux provider applies these boundaries:

- the selected root must exist, be a directory, and not itself be a symbolic link;
- resource identities are provider-relative to the selected root;
- normalized path traversal outside the selected root is rejected;
- symbolic links are visible as `SYMLINK` entries but are not traversed or mutated;
- mutation capability is withheld when a detected `FileStore`/mount boundary differs from the selected root;
- create, rename, write, copy/move, and delete primitives remain bounded by operating-system permissions and provider capabilities;
- recursive folder deletion and recursive folder transfer are refused;
- the command-line harness itself remains read-only.

The Linux desktop destination still requires a user-facing XDG location/navigation experience, mount/removable-media lifecycle and safe-eject UX, Unix ownership/permission presentation and policy, file associations/Open With, drag-and-drop, clipboard operations, keyboard/pointer navigation, windows/tabs/dual-pane behavior, network/provider locations, native accessibility, a Glaze UI desktop surface, accepted packaging, and representative runtime acceptance.

No Debian, Flatpak, AppImage, RPM, Snap, desktop environment, or distribution should be treated as supported from the current development JVM distribution.

## GoreeCloud platform status

The following GoreeCloud systems remain required platform integrations but are not yet production-accepted in File Manager:

- **GoreeCloud Drive** — first-party cloud file/resource authority.
- **GoreeCloud Sync** — synchronization and conflict authority. Sync is not backup.
- **Everkeep / GoreeCloud Backup** — continuity, backup, recovery, and preservation authority.
- **Privacy Shield** — consent, purpose limitation, minimization, exposure, and data-use authority.
- **Wardveil Security** — authoritative file/content security and protection evidence.
- **GoreeCloud Identity** — account, ownership, device, session, and delegated access authority.
- **GoreeCloud Mesh** — bounded cross-service coordination and event delivery.

Unknown, unavailable, stale, or unverified platform evidence must remain visible as such. File Manager must not turn missing evidence into a positive privacy, security, synchronization, backup, or recovery claim.

## Current limitations

The current Android development build does not yet provide complete copy/move/duplicate workflows, user-facing file creation, multi-selection, universal search, previews, tags, collections, sharing, GoreeCloud Drive runtime access, GoreeCloud Sync runtime access, unified Trash, version history, Operations Center, verified backup/Everkeep recovery, Wardveil runtime scanning evidence, Privacy Shield runtime authorization, production Identity/Mesh integration, system-wide GoreeCloud file-picker registration, production signing, store distribution, or Stable qualification.

The Linux development source does not yet provide a native desktop Glaze UI application, user-facing XDG/home-location navigation, supported mount/removable-media lifecycle or safe-eject workflows, file associations/Open With, drag-and-drop/clipboard integration, windows/tabs/dual-pane UI, network providers, accepted package formats, production signing/distribution, representative desktop/distribution acceptance, or production/Stable runtime status.

**GLAZE UI V1.3 / 1.3.0** is the current governed design-system target, but platform-specific visual, accessibility, input, responsiveness, performance, and current-Stable conformance acceptance remain required. The Linux command-line development harness is not Glaze UI conformance evidence.

## Reporting development problems

For the Android build, include the File Manager version, Android version, device model, storage-provider type, the operation attempted, expected result, and observed result.

For the Linux development harness/provider/discovery layer, include the exact source revision, Linux distribution/kernel information, Java version, selected-root filesystem type where relevant, whether a symbolic link or mount boundary was involved, the command/test attempted, expected result, and observed result.

Do not include passwords, tokens, private file contents, encryption keys, signing material, or other reusable secrets in bug reports.

## Acceptance language

A successful CI run proves only the checks performed by the workflow for the exact source revision.

Android development validation covers repository validation, shared-core tests, Android unit tests, Android lint, APK assembly, package/application-label verification, and artifact publication.

Linux development validation covers repository validation, shared-core/Linux-provider/location-discovery tests, JVM development-distribution construction, an explicit-root read-only harness smoke test, and artifact digest/boundary evidence.

Neither workflow by itself establishes production security, privacy, recovery, accessibility, representative-platform compatibility, controlled signing/deployment, Linux desktop/package support, complete mount/removable-media lifecycle behavior, or Stable qualification. Android evidence does not establish Linux acceptance and Linux development evidence does not establish Android acceptance.
