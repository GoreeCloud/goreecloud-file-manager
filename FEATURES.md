# GoreeCloud File Manager — Features

This file distinguishes **implemented now** from **target product scope**. Planned capabilities must not be read as current functionality.

## Required platform targets

GoreeCloud File Manager is required to support **Linux and Android** as first-class native platforms.

- Android is the current production-shaped native user-facing development client.
- Linux now has a shared-core-consuming local-filesystem provider, read-only location discovery, a command-line development harness, and a separate graphical desktop development surface, but **does not yet have an accepted native desktop client, supported package, production runtime, or Stable acceptance**.
- Shared file/provider identity, capability semantics, transfer verification, evidence state, and GoreeCloud authority boundaries remain coherent through the JVM `:core` module.
- Linux-native product functionality still targets complete mount/removable-media lifecycle and safe-eject behavior, Unix permissions/ownership, reviewed symbolic-link policy, file associations/Open With, clipboard and drag-and-drop, complete keyboard/pointer/accessibility behavior, windows/tabs/dual-pane workflows, and supported network/provider integration.
- Android-native behavior retains scoped/least-privilege storage, user-authorized Android document trees, touch-first adaptive layouts, Android lifecycle/permission behavior, and platform Open/Save/share integration.

## Implemented shared/core foundation

- JVM `:core` module consumed by Android and Linux development modules.
- Provider-scoped `BrowserLocation` and `FileEntry` identities.
- Explicit resource capabilities for read, child listing, create file/folder, rename, delete, copy, and move concepts.
- File, folder, and symbolic-link resource-type representation.
- Shared filename validation and provider contract.
- Provider-generic ordinary-file copy/move service.
- Streaming SHA-256 verification of source bytes plus destination reopen/readback digest before transfer success is accepted.
- Move safety that preserves the source until destination integrity is verified.
- Typed synchronization, backup, continuity, privacy, security, identity, and coordination evidence states.
- Explicit unknown/unavailable states so missing platform evidence is never promoted to positive status.
- First-party platform adapter interfaces for Drive, Sync, Backup, Everkeep, Privacy Shield, Wardveil, Identity, and Mesh.
- Shared unit tests for status-separation and transfer-integrity/failure behavior.

## Implemented Android development slice

- Native Android/Jetpack Compose application shell.
- Adaptive Home/Browse navigation foundation.
- Real browsing of the application's private local files directory.
- Android system document-tree picker integration for user-authorized storage roots.
- Persisted URI-permission discovery so authorized document trees can be restored after restart.
- Capability-driven UI that exposes only implemented and provider-supported mutation actions.
- Folder creation, rename, and non-recursive delete for supported Android providers.
- Provider-generic transfer service available to Android source, though destination-selection copy/move is not yet exposed in the UI.
- File/folder metadata for name, size, modification time, item type, provider identity, and location kind where available.
- Operation outcomes that distinguish success, rejection, and failure and refresh provider state after mutation attempts.
- Android tests for app-private mutation/path-confinement behavior.
- Android workflow definition for exact-source repository validation, shared-core tests, Android tests, lint, development APK assembly, APK identity verification, and artifact evidence.

## Implemented Linux development slice

The Linux code remains deliberately narrower than the target production desktop application.

### Provider, discovery, and headless development layer

- `:linux-client` Kotlin/JVM 17 development module consuming `:core`.
- Bounded `LinuxFileRepository` rooted at one explicitly supplied existing directory.
- Provider-relative resource identity rather than exporting Linux path strings as universal identities.
- Normalized path confinement that rejects traversal outside the selected root.
- Metadata reads with `NOFOLLOW_LINKS`.
- Symbolic links shown as `SYMLINK` resources with no traversal or mutation capability in this slice.
- Root directory cannot itself be a symbolic link.
- Mutation capability withheld when a resource is detected on a different `FileStore` from the selected root.
- Bounded create-file, create-folder, rename, ordinary-file read/write, shared verified copy/move participation, file delete, and empty-folder delete primitives where OS permissions and provider capabilities permit them.
- Recursive folder deletion and recursive folder transfer refused.
- `LinuxLocationDiscovery` read-only candidate discovery for Home, supported XDG user directories, user-facing mount points, and `/media` / `/run/media` removable-media candidates.
- XDG parsing that expands only literal `$HOME` / `${HOME}` forms or accepts explicit absolute paths; shell expressions and relative values are not executed or accepted as discovered locations.
- Location candidates set `requiresExplicitSelection = true`; discovery does not instantiate a provider, grant filesystem access, or widen the provider root.
- Mount discovery uses `/proc/self/mountinfo`, decodes mount escapes, filters pseudo/system-only mount surfaces, and does not equate a removable-media candidate with verified ejectability.
- Non-production `LinuxDevelopmentMain` command-line harness accepting one explicit root for read-only provider listing or `--locations` for read-only candidate reporting.

### Desktop presentation development layer

- Separate `:linux-desktop` presentation module using Compose Multiplatform Desktop `1.12.0`.
- `LinuxDesktopDevelopmentMain` provides a desktop-specific edge location sidebar, responsive toolbar, solid file-content plane, contextual inspector, and status/error presentation.
- `LinuxDesktopController` separates discovery/highlighting from provider authorization by construction.
- Highlighting a location candidate does not construct a provider.
- Only the explicit **Open location** action can construct the bounded `LinuxFileRepository` for the selected candidate.
- Failed open attempts leave no provider authorized in the controller.
- Folder navigation accepts only entries from the current provider ID and supports provider-scoped back navigation.
- Returning to Locations clears the active desktop provider/current-location state.
- Symbolic links remain visible while traversal stays disabled.
- The graphical development surface intentionally exposes **read-only browsing only**; provider mutation primitives are not surfaced in this milestone.
- Controller tests cover the non-authorizing discovery/highlight path, explicit provider opening, provider-scoped navigation, failed-open cleanup, and closing the provider boundary.
- Linux CI runs both repository and Linux-desktop contract validation, shared-core/Linux tests, isolated `:linux-desktop` compilation, the existing CLI development distribution, explicit-root smoke testing, and development-only artifact evidence.

A passing Linux development workflow is source/build/test evidence for this bounded slice. It is not rendered/native Glaze acceptance, supported packaging, production desktop acceptance, or justification for adding Linux to machine-readable `supported_platforms`.

## Target file-management scope

Target capabilities include complete copy/move/duplicate/create/delete/restore operations; multi-selection; drag and drop; conflict handling; list/grid/gallery/column/detail views; breadcrumbs; tabs/windows/workspaces; removable and network storage specializations; archives; permissions; checksums; duplicate handling; transfer queues; Trash; offline files; and storage cleanup/intelligence.

The current mutation slice is intentionally narrower than the target. It has verified ordinary-file copy/move service primitives but does not yet expose Android destination-selection copy/move workflows or Linux desktop mutation workflows, does not recursively transfer or delete folders, does not yet provide complete duplicate/create-file workflows, and does not claim unified Trash or recovery semantics.

## Target discovery and organization scope

Target capabilities include universal and natural-language search, content/metadata search, saved searches, tags, collections, smart collections, favorites, pinned locations, Recent, Continue Working, activity, provenance, and related-file discovery.

Linux local location-candidate discovery is now a bounded development primitive and the desktop candidate can present/select those candidates for explicit provider opening. This remains separate from search/indexing and does not itself grant provider authority.

## Target preview and details scope

Target previews include supported images, video, audio, PDF, text, code, archives, documents, spreadsheets, presentations, fonts, and other supported formats. Unified File Details is intended to combine ordinary metadata with location, sharing, permissions, sync, backup, versions, activity, privacy, security, continuity, device, and provenance state.

## Target GoreeCloud integrations

- **GoreeCloud Drive:** first-class browsing, transfer, search, sharing, ownership, version, offline, and availability state.
- **GoreeCloud Sync:** per-item/device sync state, progress, conflict handling, retry/pause/resume where authorized.
- **GoreeCloud Backup:** backup state and user-facing backup/restore workflows without conflating sync and backup.
- **Everkeep:** continuity, verified recovery, preservation, recovery points, portability, and long-term stewardship evidence.
- **Privacy Shield:** privacy/exposure context, copy-location awareness, privacy-aware indexing/search/sharing, and purpose-limited processing.
- **Wardveil Security:** authoritative security/coverage state, applicable inspection/integrity findings, and security-aware file workflows.
- **GoreeCloud Identity:** account boundaries, owners, recipients, devices, access and permission context.
- **GoreeCloud Mesh:** bounded cross-service coordination, events, state correlation, and app handoff.

## Target system experiences

Target platform experiences include a GoreeCloud-native file picker, save experience, Open With/reveal-in-file-manager flows, deep links, cross-application handoff, Operations Center, platform-level file status, and unified recovery/continuity pathways.

Linux additionally targets accepted desktop-native Glaze UI, complete keyboard/pointer/accessibility behavior, context menus, windows/tabs/dual-pane workflows, file associations, drag-and-drop, clipboard file operations, mount/removable lifecycle and safe-eject behavior, network providers, and desktop-environment interoperability.

Android additionally targets touch-first adaptive navigation, system document-provider integration, platform share/Open/Save flows, and least-privilege URI-based storage authorization.

## Not yet implemented or accepted

The current application does **not** yet provide an accepted Linux native desktop client or supported Linux package; GoreeCloud Drive connectivity; real Sync/Backup/Everkeep/Privacy/Wardveil/Identity/Mesh runtime calls; universal indexing/search; user-facing destination-selection copy/move workflows; recursive folder transfer; complete duplicate/create-file workflows; unified Trash/recovery; multi-selection; sharing; previews; network storage; removable-media lifecycle/safe-eject controls; Linux file-association/Open With or drag-and-drop/clipboard integration; complete keyboard/pointer/accessibility acceptance; windows/tabs/dual-pane UI; system file-picker registration; complete current-Stable Glaze UI acceptance; production signing/deployment; or production/Stable acceptance.
