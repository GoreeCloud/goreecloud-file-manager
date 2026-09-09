# GoreeCloud File Manager — Architecture

## 1. Native product boundary

GoreeCloud File Manager is an original GoreeCloud application. Android/Jetpack APIs, Linux platform standards/libraries, and other mature foundational components may be used, but product architecture, file-state semantics, application workflows, GoreeCloud integrations, and user experience remain GoreeCloud-owned.

Linux and Android are required first-class native product targets. Current source implementation remains Android-only; the Linux architecture described here is a required implementation target and not evidence that a Linux client already exists.

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
Android app-private / SAF          Sync / Backup / Everkeep
Linux filesystem (planned)        Privacy / Wardveil / Identity / Mesh
Drive / Network / External
```

Storage providers own resource/location mechanics. Platform authorities own their respective evidence and authorization domains. File Manager composes those results without transferring authority between them.

The architecture must keep the shared File Manager domain/provider/operation contracts independent from platform-native storage and presentation details. Android document URIs and Linux filesystem paths therefore map into one provider model without being treated as equivalent native identifiers.

## 3. Provider contract

Every storage provider exposes a `StorageProviderDescriptor`, a provider-scoped root `BrowserLocation`, child listing, and only the mutation methods it can actually support.

Each file/folder carries a provider ID plus a provider-scoped resource ID. File Manager does not assume that every resource has a normal filesystem path. This is required for Android document URIs, Linux paths with platform-specific semantics, GoreeCloud Drive resource IDs, network identifiers, and future providers.

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

The provider currently implements bounded listing and file operations represented by the current provider contract. Recursive folder deletion is rejected.

### User-authorized Android document-tree provider

`SafTreeFileRepository` consumes Android's Storage Access Framework / `DocumentsContract` after the user chooses a tree through the system picker.

File Manager requests a persistable read permission and write permission where Android/provider policy permits it. Persisted URI permissions are used to reconstruct authorized locations on future starts. File Manager does not request unrestricted filesystem access for this path.

The selected tree remains bounded by its Android tree document ID and authority. Child document URIs are accepted only when they remain inside that same tree boundary.

The provider queries each document's flags and maps them into File Manager capabilities. Mutation UI is therefore based on both persisted authorization and provider-reported support.

A selected Android document tree is intentionally not classified as ordinary local disk by default. Android DocumentsProviders may represent device storage, removable media, or remote/cloud-backed content.

## 5. Required Linux client architecture — planned, not implemented

Linux must use a native desktop application layer and a Linux filesystem/provider adapter rather than reusing Android storage APIs or translating Android URIs into fake paths.

The initial Linux filesystem provider should map authorized native filesystem resources into File Manager's existing provider-scoped identity, capability, operation-result, transfer-verification, and evidence models. It must explicitly account for:

- XDG user directories and ordinary filesystem paths;
- filesystem permissions, ownership, and metadata;
- symbolic links and canonical/real-path resolution without unsafe escape;
- mount points, removable media, disconnect/reconnect state, and safe removal where supported;
- provider capability differences across local, removable, FUSE, network, and other mounted filesystems;
- filesystem races, rename/move/copy edge cases, and partial/uncertain mutation results;
- desktop file associations and Open With behavior;
- clipboard transfers and drag-and-drop;
- keyboard and pointer navigation;
- windows, tabs, context menus, split/dual-pane workflows, and desktop density;
- native accessibility and desktop-environment interoperability.

Path traversal, symlink escape, mount-boundary, permission, ownership, and destructive-operation behavior must be validated explicitly. Linux path authority must not be widened merely because the desktop operating system exposes ordinary paths.

Exact Linux toolkit, packaging formats, and supported distribution matrix remain implementation and release decisions. No Flatpak, AppImage, Debian, RPM, Snap, desktop environment, or distribution is accepted merely because it is named as a possible delivery surface.

## 6. Cross-platform resource identity

A platform-native identifier remains scoped to its provider and platform:

```text
Linux filesystem path
≠ Android document/content URI
≠ GoreeCloud Drive resource ID
≠ network provider identifier
≠ synchronized-device identity
≠ backup/recovery record
```

File Manager must never use path-string substitution to claim that one representation is the same resource on another platform. Cross-platform access must resolve through provider identity, authorized transfers/synchronization, or bounded GoreeCloud Mesh coordination.

The shared application layer may expose common concepts—location, capability, operation, provenance, sync, backup, privacy, security, continuity—but the adapter remains authoritative for native resource mechanics.

## 7. Operation semantics

Provider mutation results use three outcomes:

- `SUCCEEDED` — the operation returned success and File Manager obtained the expected resulting state where applicable.
- `REJECTED` — File Manager or the provider deliberately refused the requested operation before treating it as successful.
- `FAILED` — execution or post-operation verification failed.

After a mutation attempt, the UI refreshes the provider state even when a result is uncertain, because a backing provider may have performed a side effect before returning an error or unverifiable response.

The current destructive-operation safety boundary deliberately refuses recursive folder deletion. File Manager has not yet implemented the unified Trash, backup/recovery, Everkeep, and operation-journal safeguards required before broad recursive destruction should be exposed.

These semantics apply to both platform targets. Linux-specific filesystem calls and Android provider calls may fail differently, but neither platform may bypass shared outcome and reconciliation rules.

## 8. File-name safety

The shared file-name policy trims surrounding whitespace and rejects empty names, `.` / `..`, path separators, null characters, and overlong names before a mutation request is issued.

This policy is a File Manager portability/safety floor. Backing providers may enforce additional naming constraints and remain authoritative for those constraints. Linux and Android adapters must preserve provider-specific naming rules rather than assuming identical filename semantics.

## 9. Unified evidence model

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

## 10. Sync versus backup

A synchronized object is a replicated working state. Synchronization can propagate deletion or corruption. Independent backup/recovery protection is a separate authority and evidence chain. Therefore:

```text
sync == synced  ≠  backup == verified recoverable
```

This invariant is represented in source and unit tests.

## 11. Platform authority boundaries

### GoreeCloud Drive

Drive is a storage/resource authority for its files, ownership, and applicable sharing/version state. File Manager consumes Drive contracts; it does not pretend Linux paths or Android document URIs are Drive resources.

### GoreeCloud Sync

Sync owns synchronization state, device replication/conflict state, and applicable synchronization controls.

### GoreeCloud Backup and Everkeep

Backup is the user-facing backup workflow/service. Everkeep is the broader continuity/resilience/preservation authority. File Manager must distinguish backup existence, integrity verification, restore testing, recovery eligibility, and preservation state.

### Privacy Shield

Privacy Shield determines privacy/consent/data-use authority. File Manager must not infer an ALLOW decision merely because an actor has operating-system resource access or is authenticated. Indexing, remote search, previews, sharing, backup, synchronization, and telemetry are separate privacy-relevant operations.

### Wardveil Security

Wardveil determines applicable security evidence and security-policy outcomes. File Manager consumes normalized Wardveil contracts, never substitutes a scanner-vendor result for Wardveil authority, and never labels missing coverage as clean/protected.

### GoreeCloud Identity

Identity owns authentication, accounts, actors/services, credentials, sessions, devices, authorization primitives, and delegated authority relevant to file access.

### GoreeCloud Mesh

Mesh coordinates bounded events/state between systems. Successful Mesh delivery is not proof that a resource operation or security/privacy/continuity decision succeeded.

### Glaze UI

GLAZE UI V1.3 / 1.3.0 is the current governed Stable consumer target. Glaze UI governs application presentation, interaction, accessibility, responsiveness, and design-system semantics. It may visualize evidence but cannot create platform truth. Each supported File Manager platform requires its own current-revision conformance evidence.

## 12. Target operation architecture

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

The current Android slice implements provider identity, capability checks, direct bounded mutations, provider-generic regular-file transfer verification, result classification, and refresh reconciliation. A durable Operations Center, complete conflict engine, Trash, and platform-authority preflight remain future work. The Linux client must adopt the same shared operation path rather than establishing a second incompatible mutation model.

## 13. Search architecture

Search will be layered: local/provider metadata search; authorized content indexing; cross-provider aggregation; saved filters/smart collections; and optional natural-language interpretation. Privacy Shield must gate collection/processing scope, and indexes must not become an uncontrolled copy of sensitive file content.

Linux and Android may use different native indexing primitives where justified, but both must emit compatible bounded search results/evidence without transferring private content into unauthorized indexes.

## 14. UI architecture

Shared information architecture, terminology, truth-state semantics, component roles, and accessibility expectations apply across Linux and Android.

The current Android shell uses Jetpack Compose/Material 3 with a repository-local Glaze mapping foundation. It has adaptive phone/wide navigation, storage-location cards, capability-driven actions, confirmation surfaces, and evidence-safe status wording.

The Linux client must provide desktop-native keyboard/pointer interaction, focus behavior, context menus, windows/tabs, drag-and-drop, larger-display density, Open With/file-association integration, and split/dual-pane workflows while retaining Glaze semantics and accessibility requirements.

Both clients require fresh migration to the current governed GLAZE UI V1.3 / 1.3.0 target and independent rendered/native acceptance. Visual similarity does not establish conformance.

## 15. Build, packaging, and acceptance split

Android and Linux are independently validated surfaces. The project must maintain clear evidence boundaries for:

- source/build identity;
- dependencies and toolchains;
- package identity and signing;
- storage/provider behavior;
- native input/accessibility behavior;
- representative device/distribution/desktop-environment coverage as applicable;
- performance and failure behavior;
- release/rollback provenance.

The current Android CI and APK evidence establishes Android development-source checks only. It cannot be reused as Linux evidence. Linux must add its own build/test/package validation before `supported_platforms` or production documentation can claim Linux as implemented.

## 16. Delivery phases

1. **Native Android foundation** — app shell, provider/evidence model, app-private browsing, CI. Completed as source/build foundation.
2. **Authorized Android storage** — user-selected document trees, persisted permissions, capability model, bounded mutations and verified regular-file transfer foundation. Implemented as current development source scope; broad acceptance remains pending.
3. **Cross-platform core separation** — preserve/extract provider/resource/operation/transfer/evidence contracts so platform-neutral logic is not coupled to Android UI/storage APIs.
4. **Linux native foundation** — Linux application shell, filesystem provider, bounded local browsing and ordinary-file operations, Linux CI, native keyboard/pointer/desktop integration.
5. **Complete local operations** — multi-selection, conflict handling, durable Operations Center, Trash/recovery strategy, metadata/details, previews, search, and recursive workflows only after safeguards are accepted.
6. **GoreeCloud storage** — Drive + Sync integration, offline state, sharing/versions, cross-location transfers.
7. **Protection and continuity** — Wardveil, Privacy Shield, Backup/Everkeep, evidence-backed destructive-action safety and recovery.
8. **Cross-device intelligence** — Identity/Mesh device context, unified activity/provenance, smart/natural-language discovery.
9. **Product acceptance** — current Glaze/Wardveil/Privacy/Everkeep gates, runtime failure testing, representative Linux and Android acceptance, signing/release/rollback, and explicitly scoped Stable qualification.
