# GoreeCloud File Manager — Features

This file distinguishes **implemented now** from **target product scope**. Planned capabilities must not be read as current functionality.

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
- File/folder metadata for name, size, modification time, item type, provider identity, and location kind where available.
- Operation outcomes that distinguish success, rejection, and failure and refresh provider state after mutation attempts.
- File-name validation for basic path safety and cross-provider portability.
- Typed synchronization, backup, continuity, privacy, security, identity, and coordination evidence states.
- Explicit unknown/unavailable states.
- First-party platform adapter interfaces for Drive, Sync, Backup, Everkeep, Privacy Shield, Wardveil, Identity, and Mesh.
- Unit tests for critical status-separation rules and app-private mutation/path-confinement behavior.
- Repository validation, lint, unit-test, and development APK CI workflow.
- Required repository and central-user-manual documentation model.

## Target file-management scope

Target capabilities include complete copy/move/duplicate/create/delete/restore operations; multi-selection; drag and drop; conflict handling; list/grid/gallery/column/detail views; breadcrumbs; tabs/windows/workspaces; removable and network storage specializations; archives; permissions; checksums; duplicate handling; transfer queues; Trash; offline files; and storage cleanup/intelligence.

The current mutation slice is intentionally narrower than the target. It does not recursively delete folders, does not yet provide copy/move/duplicate/create-file workflows, and does not claim unified Trash or recovery semantics.

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

## Not yet implemented or accepted

The current application does **not** yet provide GoreeCloud Drive connectivity; real Sync/Backup/Everkeep/Privacy/Wardveil/Identity/Mesh runtime calls; universal indexing/search; copy/move/duplicate/create-file workflows; unified Trash/recovery; multi-selection; sharing; previews; network storage; removable-media-specific controls; system file-picker registration; complete current-Stable Glaze UI acceptance; production signing/deployment; or production/Stable acceptance.
