# GoreeCloud File Manager — Architecture

## 1. Native product boundary

GoreeCloud File Manager is an original GoreeCloud application. Android/Jetpack APIs, Linux platform standards/libraries, and other mature foundational components may be used, but product architecture, file-state semantics, application workflows, GoreeCloud integrations, and user experience remain GoreeCloud-owned.

Linux and Android are required first-class native product targets. Android is the current implemented native user-facing client. Linux source now establishes a bounded local-filesystem provider, a read-only location-candidate discovery layer, and a non-production JVM development harness, but those components are not the accepted native desktop application, supported package, or production runtime.

## 2. Layer model

```text
Glaze UI application surfaces
        ↓
File Manager application/use cases
        ↓
Shared JVM :core
  ├─ provider/resource identity + capability model
  ├─ file/status/evidence model
  ├─ filename policy
  ├─ verified transfer service
  └─ GoreeCloud authority adapter contracts
        ↓
Platform discovery/adapters ───────── Platform authority adapters
        ↓                                      ↓
Android app-private / SAF                 Sync / Backup / Everkeep
Linux read-only location discovery        Privacy / Wardveil / Identity / Mesh
Linux bounded filesystem provider
Drive / Network / External (target)
```

Storage providers own resource/location mechanics after authorization. Platform discovery may identify candidate locations but does not create provider authority. Platform authorities own their respective evidence and authorization domains. File Manager composes those results without transferring authority between them.

The architecture keeps shared File Manager domain/provider/operation contracts independent from platform-native storage and presentation details. Android document URIs and Linux filesystem paths map into one provider model without being treated as equivalent native identifiers.

The current Linux development harness intentionally sits below the eventual desktop presentation layer. It exists to exercise the Linux discovery/provider/build boundaries while the production desktop Glaze UI toolkit and packaging model remain unaccepted.

## 3. Provider contract

Every storage provider exposes a `StorageProviderDescriptor`, a provider-scoped root `BrowserLocation`, child listing, and only the mutation methods it can actually support.

Each resource carries a provider ID plus a provider-scoped resource ID. File Manager does not assume that every resource has a normal filesystem path. This is required for Android document URIs, Linux paths with platform-specific semantics, GoreeCloud Drive resource IDs, network identifiers, and future providers.

A Linux discovery candidate is not yet a provider resource. It becomes eligible for provider construction only after an explicit selection/authorization step appropriate to the desktop workflow. Discovery alone must not grant read, mutation, or traversal capability.

The shared resource type distinguishes files, folders, and symbolic links. A symbolic-link representation is not itself permission to follow the target.

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

The provider currently implements bounded listing and file operations represented by the shared provider contract. Recursive folder deletion is rejected.

### User-authorized Android document-tree provider

`SafTreeFileRepository` consumes Android's Storage Access Framework / `DocumentsContract` after the user chooses a tree through the system picker.

File Manager requests a persistable read permission and write permission where Android/provider policy permits it. Persisted URI permissions are used to reconstruct authorized locations on future starts. File Manager does not request unrestricted filesystem access for this path.

The selected tree remains bounded by its Android tree document ID and authority. Child document URIs are accepted only when they remain inside that same tree boundary.

The provider queries each document's flags and maps them into File Manager capabilities. Mutation UI is therefore based on both persisted authorization and provider-reported support.

A selected Android document tree is intentionally not classified as ordinary local disk by default. Android DocumentsProviders may represent device storage, removable media, or remote/cloud-backed content.

## 5. Linux client architecture — current development boundary and required destination

Linux uses its own filesystem/provider adapter rather than reusing Android storage APIs or translating Android URIs into fake paths.

### Current bounded provider

`LinuxFileRepository` is the first source implementation of the Linux provider boundary. It is rooted at one explicitly supplied existing directory and maps Linux resources into the same shared provider-scoped identity, capability, operation-result, transfer-verification, and evidence model used by Android.

Current safety behavior is deliberately conservative:

- the selected root is normalized, must be a directory, and cannot itself be a symbolic link;
- provider resource IDs are relative to that selected root;
- normalized resource candidates that escape the root are rejected;
- metadata is inspected with `LinkOption.NOFOLLOW_LINKS`;
- symbolic links are visible as `SYMLINK` resources but receive no traversal or mutation capabilities in this slice;
- provider traversal through a symbolic-link component is rejected;
- ordinary reads depend on operating-system readability;
- create, rename, write, move-source deletion, and delete are permitted only where provider capability and operating-system writability allow them;
- mutation capability is withheld when the resource resolves to a different `FileStore` than the selected root, preventing mount boundaries from silently broadening mutation authority before an accepted policy exists;
- the provider root cannot be renamed or deleted;
- recursive folder deletion is rejected; only empty folders may be deleted;
- recursive folder transfer remains rejected by the shared transfer service;
- ordinary files participate in the shared SHA-256-verified transfer service.

This provider does not establish unrestricted Linux filesystem authority. The explicit root is a development scope, not an entitlement to crawl or mutate the rest of the host.

### Current read-only location discovery

`LinuxLocationDiscovery` is a pre-provider metadata layer. Its purpose is to identify conservative desktop location **candidates** without silently turning discovery into filesystem authorization.

Current discovery behavior:

- reports the current Home directory only when it exists as a non-symlink directory;
- reads recognized XDG user-directory keys from `user-dirs.dirs` beneath an absolute `XDG_CONFIG_HOME` or the `$HOME/.config` fallback;
- expands only literal `$HOME` / `${HOME}` XDG values and accepts explicit absolute paths;
- rejects relative XDG paths and does not execute backticks, `$()` expressions, or other shell-variable forms;
- parses `/proc/self/mountinfo`, including Linux octal mount-token escapes;
- filters ordinary pseudo/system-only filesystem types and non-user-facing mount surfaces from the candidate set;
- labels `/media` and `/run/media` paths as `REMOVABLE_MEDIA_CANDIDATE` rather than claiming confirmed hardware removability or ejectability;
- includes only existing non-symlink candidate directories under the current checks;
- collapses duplicate normalized candidate paths;
- marks every candidate `requiresExplicitSelection = true`.

Discovery does **not** construct `LinuxFileRepository`, create a `StorageProviderDescriptor`, grant read/write access, traverse candidate contents, authorize mutations, or make a removable-media safe-eject claim. The eventual desktop application must preserve that discovery-versus-authorization boundary.

### Current development harness

`LinuxDevelopmentMain` is a non-production command-line harness. Explicit-root mode accepts one selected root and exposes a read-only listing through `LinuxFileRepository`. `--locations` exposes only the read-only location-candidate report and does not create a provider. The harness deliberately does not expose create/rename/delete/transfer commands even though provider primitives are testable; destructive user-facing Linux workflows require a real desktop UX, conflict handling, recovery context, and platform acceptance.

### Required desktop destination

The native Linux client still must add:

- user-facing Home/XDG location presentation, selection, navigation, and persistence policy over the current read-only discovery foundation;
- filesystem permissions, ownership, ACL/security metadata presentation and policy where applicable;
- reviewed symbolic-link navigation behavior if future requirements justify following links;
- authoritative mount/removable-device state, disconnect/reconnect handling, safe removal/eject behavior, and explicit mount-boundary UX beyond current mount-candidate discovery;
- capability differences across local, removable, FUSE, network, and other mounted filesystems;
- filesystem-race handling and durable operation reconciliation;
- desktop file associations and Open With behavior;
- clipboard transfers and drag-and-drop;
- keyboard and pointer navigation;
- windows, tabs, context menus, split/dual-pane workflows, and desktop density;
- native accessibility and desktop-environment interoperability;
- an accepted production desktop Glaze UI implementation;
- accepted packaging and supported-environment boundaries.

Exact Linux toolkit, packaging formats, and supported distribution matrix remain implementation and release decisions. No Flatpak, AppImage, Debian, RPM, Snap, desktop environment, or distribution is accepted merely because it is named as a possible delivery surface. The current Gradle/JVM distribution is development evidence, not a production package commitment.

## 6. Cross-platform resource identity

A platform-native identifier remains scoped to its provider and platform:

```text
Linux discovery candidate path
≠ authorized Linux provider/resource identity
≠ Android document/content URI
≠ GoreeCloud Drive resource ID
≠ network provider identifier
≠ synchronized-device identity
≠ backup/recovery record
```

File Manager must never use path-string substitution to claim that one representation is the same resource on another platform. A discovered Linux path is also not automatically equivalent to a File Manager provider resource. Cross-platform access must resolve through provider identity, authorized transfers/synchronization, or bounded GoreeCloud Mesh coordination.

The shared application layer exposes common concepts—location, capability, operation, provenance, sync, backup, privacy, security, continuity—but the adapter remains authoritative for native resource mechanics.

## 7. Operation semantics

Provider mutation results use three outcomes:

- `SUCCEEDED` — the operation returned success and File Manager obtained the expected resulting state where applicable.
- `REJECTED` — File Manager or the provider deliberately refused the requested operation before treating it as successful.
- `FAILED` — execution or post-operation verification failed.

After a mutation attempt, a user-facing platform layer must reconcile provider state rather than assuming an error proves that no side effect occurred.

The current destructive-operation safety boundary deliberately refuses recursive folder deletion on both Android and Linux development providers. File Manager has not yet implemented the unified Trash, backup/recovery, Everkeep, and operation-journal safeguards required before broad recursive destruction should be exposed.

These semantics apply to both platform targets. Linux filesystem calls and Android provider calls fail differently, but neither platform may bypass shared outcome and reconciliation rules.

## 8. Verified ordinary-file transfer

`FileTransferService` lives in `:core` and accepts registered `FileStorageProvider` instances.

For ordinary-file copy/move it:

1. validates the requested filename;
2. checks source `COPY`/`MOVE` and destination `CREATE_FILE` capability;
3. rejects recursive folder transfer;
4. creates the destination through its provider;
5. streams the source while computing SHA-256;
6. reopens the resulting destination and computes a second SHA-256 digest;
7. accepts copy success only when the digests match;
8. removes a failed/corrupt destination when possible;
9. for move, deletes the source only after verified destination integrity;
10. if source deletion fails after verified copy, retains both resources and reports failure rather than pretending the move completed.

This service is shared by Android and Linux provider code. Linux tests exercise cross-provider Linux ordinary-file copy/move through the same implementation; Android UI destination selection remains future work.

## 9. File-name safety

The shared file-name policy trims surrounding whitespace and rejects empty names, `.` / `..`, path separators, null characters, and overlong names before a mutation request is issued.

This policy is a File Manager portability/safety floor. Backing providers may enforce additional naming constraints and remain authoritative for those constraints. Linux and Android adapters preserve provider-specific naming rules rather than assuming identical filename semantics.

## 10. Unified evidence model

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

## 11. Sync versus backup

A synchronized object is a replicated working state. Synchronization can propagate deletion or corruption. Independent backup/recovery protection is a separate authority and evidence chain. Therefore:

```text
sync == synced  ≠  backup == verified recoverable
```

This invariant is represented in shared source and unit tests.

## 12. Platform authority boundaries

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

The Linux command-line development harness is not a Glaze UI implementation and must not be counted as desktop UI conformance evidence.

## 13. Target operation architecture

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

For Linux, desktop location discovery may precede this path but does not replace the explicit provider-selection/authorization step. No mutation may begin directly from a discovery candidate.

The current Android slice implements provider identity, capability checks, direct bounded mutations, provider-generic ordinary-file transfer verification, result classification, and refresh reconciliation. The current Linux provider implements the storage-side provider/capability/transfer foundation while location discovery remains metadata-only; neither currently exposes a desktop mutation workflow. A durable Operations Center, complete conflict engine, Trash, and platform-authority preflight remain future work.

## 14. Search architecture

Search will be layered: local/provider metadata search; authorized content indexing; cross-provider aggregation; saved filters/smart collections; and optional natural-language interpretation. Privacy Shield must gate collection/processing scope, and indexes must not become an uncontrolled copy of sensitive file content.

Linux and Android may use different native indexing primitives where justified, but both must emit compatible bounded search results/evidence without transferring private content into unauthorized indexes.

## 15. UI architecture

Shared information architecture, terminology, truth-state semantics, component roles, and accessibility expectations apply across Linux and Android.

The current Android shell uses Jetpack Compose/Material 3 with a repository-local Glaze mapping foundation. It has adaptive phone/wide navigation, storage-location cards, capability-driven actions, confirmation surfaces, and evidence-safe status wording.

The Linux development harness currently has no production UI layer. Its `--locations` report is engineering evidence, not the desktop Home/location surface. The future desktop client must provide desktop-native Home/XDG/mount presentation and explicit location selection, keyboard/pointer interaction, focus behavior, context menus, windows/tabs, drag-and-drop, larger-display density, Open With/file-association integration, and split/dual-pane workflows while retaining Glaze semantics and accessibility requirements.

Both native user-facing clients require fresh migration to the current governed GLAZE UI V1.3 / 1.3.0 target and independent rendered/native acceptance. Visual similarity does not establish conformance.

## 16. Build, packaging, and acceptance split

Android and Linux are independently validated surfaces. The project maintains separate evidence boundaries for:

- source/build identity;
- dependencies and toolchains;
- package/distribution identity and signing;
- storage/provider behavior;
- location-discovery behavior where applicable;
- native input/accessibility behavior;
- representative device/distribution/desktop-environment coverage as applicable;
- performance and failure behavior;
- release/rollback provenance.

The Android workflow checks repository contracts, shared-core tests, Android tests, lint, development APK assembly, package/application identity, and APK artifact evidence.

The Linux development workflow checks repository contracts, shared-core tests, Linux provider/location-discovery tests, JVM development distribution construction, explicit-root harness smoke behavior, and a distribution digest. Its artifact is explicitly development-only and cannot be presented as an accepted Linux package or Stable release.

Only a completed workflow for the exact candidate revision is evidence for that candidate. Android evidence cannot be reused as Linux evidence, and a Linux development build cannot be upgraded into desktop/package/runtime acceptance without the missing platform-specific gates.

## 17. Delivery phases

1. **Native Android foundation** — app shell, provider/evidence model, app-private browsing, CI. Completed as source/build foundation.
2. **Authorized Android storage** — user-selected document trees, persisted permissions, capability model, bounded mutations and verified ordinary-file transfer foundation. Implemented as current development source scope; broad acceptance remains pending.
3. **Cross-platform core separation** — extract provider/resource/operation/transfer/evidence contracts so platform-neutral logic is not coupled to Android UI/storage APIs. Implemented in merged development source through `:core`; platform acceptance remains separate.
4. **Linux native foundation** — currently partial: bounded Linux filesystem provider, read-only Home/XDG/mount location-candidate discovery, shared transfer integration, tests, command-line development harness, and Linux CI definition are present. Native desktop Glaze UI, user-facing location selection/navigation, authoritative mount/removable lifecycle and safe eject, desktop input/accessibility, accepted packaging, and representative runtime acceptance remain pending.
5. **Complete local operations** — multi-selection, conflict handling, durable Operations Center, Trash/recovery strategy, metadata/details, previews, search, and recursive workflows only after safeguards are accepted.
6. **GoreeCloud storage** — Drive + Sync integration, offline state, sharing/versions, cross-location transfers.
7. **Protection and continuity** — Wardveil, Privacy Shield, Backup/Everkeep, evidence-backed destructive-action safety and recovery.
8. **Cross-device intelligence** — Identity/Mesh device context, unified activity/provenance, smart/natural-language discovery.
9. **Product acceptance** — current Glaze/Wardveil/Privacy/Everkeep gates, runtime failure testing, representative Linux and Android acceptance, signing/release/rollback, and explicitly scoped Stable qualification.
