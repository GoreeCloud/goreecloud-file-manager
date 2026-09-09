# GoreeCloud File Manager — Features

This file distinguishes **implemented now** from **target product scope**. Planned capabilities must not be read as current functionality.

## Required platform targets

GoreeCloud File Manager is required to support **Linux and Android** as first-class native platforms.

- Android is the current implemented native user-facing development client.
- Linux now has a shared-core-consuming local-filesystem development provider and command-line build harness, but **does not yet have the accepted native desktop client, supported package, production runtime, or Stable acceptance**.
- Shared file/provider identity, capability semantics, transfer verification, evidence state, and GoreeCloud authority boundaries remain coherent across both code paths through the JVM `:core` module.
- Linux-native product functionality still targets XDG locations, mounts/removable storage, Unix permissions/ownership, symbolic-link policy, file associations/Open With, drag-and-drop, keyboard/pointer workflows, windows/tabs/dual-pane behavior, and supported network/provider integration.
- Android-native behavior retains scoped/least-privilege storage, user-authorized Android document trees, touch-first adaptive layouts, Android lifecycle/permission behavior, and platform Open/Save/share integration.

## Implemented shared/core foundation

- JVM `:core` module consumed by Android and the Linux development module.
- Provider-scoped `BrowserLocation` and `FileEntry` identities.
- Explicit resource capabilities for read, child listing, create file/folder, rename, delete, copy, and move concepts.
- File, folder, and symbolic-link resource-type representation.
- Shared filename validation and provider contract.
- Provider-generic ordinary-file copy/move service.
- Streaming SHA-256 verification of the exact source bytes written plus destination reopen/readback digest before transfer success is accepted.
- Move safety that preserves the source until destination integrity is verified; same-size corruption is rejected and the bad destination is removed when possible.
- Typed synchronization, backup, continuity, privacy, security, identity, and coordination evidence states.
- Explicit unknown/unavailable states so missing platform evidence is never promoted to positive status.
- First-party platform adapter interfaces for Drive, Sync, Backup, Everkeep, Privacy Shield, Wardveil, Identity, and Mesh.
- Shared unit tests for status-separation and transfer-integrity/failure behavior.

## Implemented Android development slice

- Native Android/Jetpack Compose application shell.
- Adaptive Home/Browse navigation foundation.
- Real browsing of the application's private local files directory.
- Android system document-tree picker integration for user-authorized storage roots.
- Persisted URI-permission discovery so authorized document trees can be restored after application restart.
- Capability-driven UI that exposes only implemented and provider-supported mutation actions.
- Folder creation in app-private storage and supported Android document-tree providers.
- Rename in app-private storage and supported Android document-tree providers.
- Delete in app-private storage and supported Android document-tree providers, with recursive folder deletion deliberately refused.
- Provider-generic transfer service available to the Android source layer, though destination-selection copy/move is not yet exposed in the user interface.
- File/folder metadata for name, size, modification time, item type, provider identity, and location kind where available.
- Operation outcomes that distinguish success, rejection, and failure and refresh provider state after mutation attempts.
- Android tests for app-private mutation/path-confinement behavior.
- Android workflow definition for exact-source repository validation, shared-core tests, Android tests, lint, development APK assembly, APK identity verification, and artifact evidence.

## Implemented Linux development slice

The Linux code is deliberately narrower than the target desktop application.

- `:linux-client` Kotlin/JVM 17 development module consuming `:core`.
- Bounded `LinuxFileRepository` rooted at one explicitly supplied existing directory.
- Provider-relative resource identity rather than exporting a Linux path string as a universal identity.
- Normalized path confinement that rejects provider-relative traversal outside the selected root.
- Metadata reads with `NOFOLLOW_LINKS`.
- Symbolic links shown as `SYMLINK` resources with no traversal or mutation capability in this slice.
- Root directory cannot itself be a symbolic link.
- Mutation capability withheld when a resource is detected on a different `FileStore` from the selected root, pending accepted mount-boundary policy.
- Bounded create-file, create-folder, rename, ordinary-file read/write, shared verified copy/move participation, file delete, and empty-folder delete primitives where OS permissions and provider capabilities permit them.
- Recursive folder deletion refused.
- Recursive folder transfer refused by the shared transfer service.
- Non-production `LinuxDevelopmentMain` command-line harness accepting one explicit root and exposing read-only listing only.
- Linux provider tests for symlink visibility/non-traversal, path-escape rejection, root mutation refusal, non-recursive deletion, and shared verified Linux-to-Linux ordinary-file transfer.
- Linux workflow definition for exact-source repository validation, shared-core/Linux tests, JVM development distribution build, explicit-root smoke testing, artifact digest, and a development-only packaging boundary.

A passing Linux development workflow is source/build/test evidence for this bounded slice; it is not production desktop acceptance and does not by itself justify adding Linux to machine-readable `supported_platforms`.

## Target file-management scope

Target capabilities include complete copy/move/duplicate/create/delete/restore operations; multi-selection; drag and drop; conflict handling; list/grid/gallery/column/detail views; breadcrumbs; tabs/windows/workspaces; removable and network storage specializations; archives; permissions; checksums; duplicate handling; transfer queues; Trash; offline files; and storage cleanup/intelligence.

The current mutation slice is intentionally narrower than the target. It has verified ordinary-file copy/move service primitives but does not yet expose destination-selection copy/move workflows in the Android UI or a Linux desktop UI, does not recursively transfer or delete folders, does not yet provide complete duplicate/create-file workflows, and does not claim unified Trash or recovery semantics.

The Linux desktop client must continue from the bounded provider foundation into an accessible desktop UI and platform-native integration before broader recursive/destructive workflows are enabled.

## Target discovery and organization scope

Target capabilities include universal and natural-language search, content/metadata search, saved searches, tags, collections, smart collections, favorites, pinned locations, Recent, Continue Working, activity, provenance, and related-file discovery.

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

Target platform experiences include a GoreeCloud-native file picker, save experience, open-with/reveal-in-file-manager flows, deep links, cross-application handoff, Operations Center, platform-level file status, and unified recovery/continuity pathways.

Linux additionally targets desktop-native Glaze UI, keyboard/pointer navigation, context menus, windows/tabs/dual-pane workflows, file associations, drag-and-drop, clipboard file operations, XDG integration, removable/mounted storage behavior, network providers, and desktop-native accessibility.

Android additionally targets touch-first adaptive navigation, system document-provider integration, platform share/Open/Save flows, and least-privilege URI-based storage authorization.

## Not yet implemented or accepted

The current application does **not** yet provide an accepted Linux native desktop client or supported Linux package; GoreeCloud Drive connectivity; real Sync/Backup/Everkeep/Privacy/Wardveil/Identity/Mesh runtime calls; universal indexing/search; user-facing destination-selection copy/move workflows; recursive folder transfer; complete duplicate/create-file workflows; unified Trash/recovery; multi-selection; sharing; previews; network storage; removable-media-specific controls; Linux XDG/file-association/Open With/drag-and-drop/desktop-window integration; system file-picker registration; complete current-Stable Glaze UI acceptance; production signing/deployment; or production/Stable acceptance.
