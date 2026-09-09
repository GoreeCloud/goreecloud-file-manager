# GoreeCloud File Manager — Features

This file distinguishes **implemented now** from **target product scope**. Planned capabilities must not be read as current functionality.

## Required platform targets

GoreeCloud File Manager is required to support **Linux and Android** as first-class native platforms.

- Android is the current implemented development client.
- Linux is a required target but is **not yet implemented, packaged, runtime-validated, or accepted**.
- Shared file/provider identity, capability semantics, transfer verification, evidence state, and GoreeCloud authority boundaries must remain coherent across both platforms.
- Linux-native functionality targets authorized filesystem paths, XDG locations, mounts/removable storage, Unix permissions/ownership, symbolic links, file associations/Open With, drag-and-drop, keyboard/pointer workflows, windows/tabs/dual-pane behavior, and supported network/provider integration.
- Android-native behavior retains scoped/least-privilege storage, user-authorized Android document trees, touch-first adaptive layouts, Android lifecycle/permission behavior, and platform Open/Save/share integration.

## Implemented in the current Android development slice

- Native Android application shell.
- Adaptive Home/Browse navigation foundation.
- Real browsing of the application's private local files directory.
- Android system document-tree picker integration for user-authorized storage roots.
- Persisted URI-permission discovery so authorized document trees can be restored after application restart.
- Provider-scoped browser locations and resource identities rather than assuming every item has a normal filesystem path.
- Explicit per-item storage capabilities for read, child listing, create file/folder, rename, delete, copy, and move concepts.
- Capability-driven UI that exposes only implemented and provider-supported mutation actions.
- Folder creation in app-private storage and supported Android document-tree providers.
- Rename in app-private storage and supported Android document-tree providers.
- Delete in app-private storage and supported Android document-tree providers, with recursive folder deletion deliberately refused.
- Provider-generic regular-file copy/move service across registered providers.
- Streaming SHA-256 verification of the exact source bytes written plus a destination reopen/readback digest before copy success is accepted.
- Move safety that preserves the source until destination integrity is verified; same-size corruption is rejected and the bad destination is removed when possible.
- File/folder metadata for name, size, modification time, item type, provider identity, and location kind where available.
- Operation outcomes that distinguish success, rejection, and failure and refresh provider state after mutation attempts.
- File-name validation for basic path safety and cross-provider portability.
- Typed synchronization, backup, continuity, privacy, security, identity, and coordination evidence states.
- Explicit unknown/unavailable states.
- First-party platform adapter interfaces for Drive, Sync, Backup, Everkeep, Privacy Shield, Wardveil, Identity, and Mesh.
- Unit tests for critical status-separation rules, app-private mutation/path-confinement behavior, and regular-file transfer integrity/failure handling.
- Repository validation, lint, unit-test, and development APK CI workflow.
- Required repository and central-user-manual documentation model.

## Target file-management scope

Target capabilities include complete copy/move/duplicate/create/delete/restore operations; multi-selection; drag and drop; conflict handling; list/grid/gallery/column/detail views; breadcrumbs; tabs/windows/workspaces; removable and network storage specializations; archives; permissions; checksums; duplicate handling; transfer queues; Trash; offline files; and storage cleanup/intelligence.

The current mutation slice is intentionally narrower than the target. It has verified regular-file copy/move service primitives but does not yet expose destination-selection copy/move workflows in the Android UI, does not recursively transfer or delete folders, does not yet provide complete duplicate/create-file workflows, and does not claim unified Trash or recovery semantics.

The Linux client must begin with bounded local browsing and safe ordinary-file operations before broader recursive/destructive workflows are enabled.

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

Linux additionally targets desktop-native keyboard/pointer navigation, context menus, windows/tabs/dual-pane workflows, file associations, drag-and-drop, removable/mounted storage behavior, and desktop-native accessibility.

Android additionally targets touch-first adaptive navigation, system document-provider integration, platform share/Open/Save flows, and least-privilege URI-based storage authorization.

## Not yet implemented or accepted

The current application does **not** yet provide a Linux client; GoreeCloud Drive connectivity; real Sync/Backup/Everkeep/Privacy/Wardveil/Identity/Mesh runtime calls; universal indexing/search; user-facing destination-selection copy/move workflows; recursive folder transfer; complete duplicate/create-file workflows; unified Trash/recovery; multi-selection; sharing; previews; network storage; removable-media-specific controls; system file-picker registration; complete current-Stable Glaze UI acceptance; production signing/deployment; or production/Stable acceptance.
