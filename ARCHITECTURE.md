# GoreeCloud File Manager — Architecture

## 1. Native product boundary

GoreeCloud File Manager is an original GoreeCloud application. Android/Jetpack APIs and other mature foundational components may be used, but product architecture, file-state semantics, application workflows, GoreeCloud integrations, and user experience remain GoreeCloud-owned.

## 2. Layer model

```text
Glaze UI application surfaces
        ↓
File Manager application/use cases
        ↓
Unified file + provider capability + operation/evidence model
        ↓
Storage/provider adapters ───── Platform authority adapters
        ↓                               ↓
App-private / Android SAF          Sync / Backup / Everkeep
External / Drive / Network         Privacy / Wardveil / Identity / Mesh
```

Storage providers own resource/location mechanics. Platform authorities own their respective evidence and authorization domains. File Manager composes those results without transferring authority between them.

## 3. Provider contract

Every storage provider exposes a `StorageProviderDescriptor`, a provider-scoped root `BrowserLocation`, child listing, and only the mutation methods it can actually support.

Each file/folder carries a provider ID plus a provider-scoped resource ID. File Manager does not assume that every resource has a normal filesystem path. This is required for Android document URIs, GoreeCloud Drive resource IDs, network identifiers, and future providers.

Per-resource capabilities currently model:

```text
READ
LIST_CHILDREN
CREATE_FILE
CREATE_FOLDER
RENAME
DELETE
COPY
MOVE
```

A capability is not a promise that every provider operation will succeed. It is a precondition for presenting the operation. The provider remains authoritative for execution and must return success, rejection, or failure without manufacturing certainty.

## 4. Current Android providers

### App-private provider

`LocalFileRepository` is confined to the application's canonical private files root. Every requested filesystem resource is canonicalized and rejected if it escapes that root.

The provider currently implements listing, create-folder, rename, and delete. Recursive folder deletion is rejected. Copy, move, duplicate, and create-file are not advertised yet.

### User-authorized Android document-tree provider

`SafTreeFileRepository` consumes Android's Storage Access Framework / `DocumentsContract` after the user chooses a tree through the system picker.

File Manager requests a persistable read permission and write permission where Android/provider policy permits it. Persisted URI permissions are used to reconstruct authorized locations on future starts. File Manager does not request unrestricted filesystem access for this path.

The selected tree remains bounded by its Android tree document ID and authority. Child document URIs are accepted only when they remain inside that same tree boundary.

The provider queries each document's flags and maps them into File Manager capabilities. Mutation UI is therefore based on both persisted authorization and provider-reported support.

A selected Android document tree is intentionally not classified as ordinary local disk by default. Android DocumentsProviders may represent device storage, removable media, or remote/cloud-backed content.

## 5. Operation semantics

Provider mutation results use three outcomes:

- `SUCCEEDED` — the operation returned success and File Manager obtained the expected resulting state where applicable.
- `REJECTED` — File Manager or the provider deliberately refused the requested operation before treating it as successful.
- `FAILED` — execution or post-operation verification failed.

After a mutation attempt, the UI refreshes the provider state even when a result is uncertain, because a backing provider may have performed a side effect before returning an error or unverifiable response.

The current destructive-operation safety boundary deliberately refuses recursive folder deletion. File Manager has not yet implemented the unified Trash, backup/recovery, Everkeep, and operation-journal safeguards required before broad recursive destruction should be exposed.

## 6. File-name safety

The shared file-name policy trims surrounding whitespace and rejects empty names, `.` / `..`, path separators, null characters, and overlong names before a mutation request is issued.

This policy is a File Manager portability/safety floor. Backing providers may enforce additional naming constraints and remain authoritative for those constraints.

## 7. Unified evidence model

File Manager composes separate evidence dimensions. At minimum:

```text
storage/location
local availability
sync state + evidence
backup/recoverability state + evidence
Everkeep continuity/preservation state + evidence
sharing/access
Privacy Shield state + evidence
Wardveil state + evidence
identity/device context
version/provenance/activity
```

No dimension can silently manufacture another. The UI must preserve `unknown`, `unavailable`, `pending`, expired, and unverified states where applicable.

## 8. Sync versus backup

A synchronized object is a replicated working state. Synchronization can propagate deletion or corruption. Independent backup/recovery protection is a separate authority and evidence chain. Therefore:

```text
sync == synced  ≠  backup == verified recoverable
```

This invariant is represented in source and unit tests.

## 9. Platform authority boundaries

### GoreeCloud Drive

Drive is a storage/resource authority for its files, ownership, and applicable sharing/version state. File Manager consumes Drive contracts; it does not pretend local paths or Android document URIs are Drive resources.

### GoreeCloud Sync

Sync owns synchronization state, device replication/conflict state, and applicable synchronization controls.

### GoreeCloud Backup and Everkeep

Backup is the user-facing backup workflow/service. Everkeep is the broader continuity/resilience/preservation authority. File Manager must distinguish backup existence, integrity verification, restore testing, recovery eligibility, and preservation state.

### Privacy Shield

Privacy Shield determines privacy/consent/data-use authority. File Manager must not infer an ALLOW decision merely because an actor is authenticated. Indexing, remote search, previews, sharing, backup, synchronization, and telemetry are separate privacy-relevant operations.

### Wardveil Security

Wardveil determines applicable security evidence and security-policy outcomes. File Manager consumes normalized Wardveil contracts, never substitutes a scanner-vendor result for Wardveil authority, and never labels missing coverage as clean/protected.

### GoreeCloud Identity

Identity owns authentication, accounts, actors/services, credentials, sessions, devices, authorization primitives, and delegated authority relevant to file access.

### GoreeCloud Mesh

Mesh coordinates bounded events/state between systems. Successful Mesh delivery is not proof that a resource operation or security/privacy/continuity decision succeeded.

### Glaze UI

Glaze UI governs application presentation, interaction, accessibility, responsiveness, and design-system semantics. It may visualize evidence but cannot create platform truth.

## 10. Target operation architecture

The complete mutation path remains:

```text
user/app intent
→ resolve exact provider + resource identity
→ obtain applicable identity/privacy/security authorization/evidence
→ validate provider capability + operation preconditions
→ enqueue/journal operation
→ provider executes or rejects
→ verify authoritative resulting state
→ reconcile if outcome certainty is lost
→ update activity/provenance and bounded Mesh events
```

The current Android slice implements provider identity, capability checks, direct create-folder/rename/delete execution, result classification, and refresh reconciliation. A durable Operations Center, copy/move journal, conflict engine, Trash, and platform-authority preflight remain future work.

## 11. Search architecture

Search will be layered: local/provider metadata search; authorized content indexing; cross-provider aggregation; saved filters/smart collections; and optional natural-language interpretation. Privacy Shield must gate collection/processing scope, and indexes must not become an uncontrolled copy of sensitive file content.

## 12. UI architecture

The Android shell targets Glaze UI 2.0.0 semantics through application-local tokens and Material 3/Compose primitives. It has adaptive phone/wide navigation, storage-location cards, capability-driven actions, confirmation surfaces, and evidence-safe status wording.

This remains an adoption foundation. Full rendered/native accessibility, responsive/form-factor behavior, representative-device testing, and current-Stable Glaze UI acceptance are pending.

## 13. Delivery phases

1. **Native foundation** — app shell, provider/evidence model, app-private browsing, CI. Completed as source/build foundation.
2. **Authorized Android storage** — user-selected document trees, persisted permissions, capability model, initial create/rename/delete operations. Current development slice.
3. **Complete local operations** — create-file, copy/move/duplicate, multi-selection, conflict handling, durable Operations Center, Trash/recovery strategy, metadata/details, previews, search.
4. **GoreeCloud storage** — Drive + Sync integration, offline state, sharing/versions, cross-location transfers.
5. **Protection and continuity** — Wardveil, Privacy Shield, Backup/Everkeep, evidence-backed destructive-action safety and recovery.
6. **Cross-device intelligence** — Identity/Mesh device context, unified activity/provenance, smart/natural-language discovery.
7. **Product acceptance** — complete current Glaze/Wardveil/Privacy/Everkeep gates, runtime failure testing, representative-device acceptance, signing/release/rollback, and Stable qualification.
