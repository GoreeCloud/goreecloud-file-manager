# GoreeCloud File Manager — Architecture

## 1. Native product boundary

GoreeCloud File Manager is an original GoreeCloud-owned application. Android/Jetpack APIs, Linux platform standards/libraries, Compose Multiplatform Desktop, and other mature foundational components may be used, but product architecture, file-state semantics, workflows, GoreeCloud integrations, and user experience remain GoreeCloud-owned.

Linux and Android are required first-class native product targets. Android is the current production-shaped native user-facing development client. Linux now has a bounded provider/discovery/controller layer plus a separate graphical desktop development presentation module. Neither the presence of Linux source nor successful development CI establishes a supported Linux package, production runtime, or Stable acceptance.

## 2. Layer model

```text
Platform-native presentation
  Android :app                     Linux :linux-desktop
  Jetpack Compose                  Compose Multiplatform Desktop
        ↓                                  ↓
Platform application/adapters
  Android storage adapters         Linux :linux-client
                                    ├─ location discovery
                                    ├─ desktop controller
                                    ├─ bounded filesystem provider
                                    └─ CLI development harness
        ↓                                  ↓
                 Shared JVM :core
                 ├─ provider/resource identity
                 ├─ capability + operation outcomes
                 ├─ file/status/evidence model
                 ├─ filename policy
                 ├─ verified transfer service
                 └─ GoreeCloud authority adapter contracts
                               ↓
            GoreeCloud platform authorities
      Drive / Sync / Backup / Everkeep / Privacy Shield
             Wardveil / Identity / Mesh
```

The architecture keeps shared File Manager contracts independent from platform-native storage and presentation details. Android document/content URIs and Linux filesystem paths remain native identifiers scoped to their providers; they are not interchangeable representations of one universal path.

## 3. Provider contract

Every storage provider exposes a `StorageProviderDescriptor`, a provider-scoped root `BrowserLocation`, child listing, and only the operations it can actually support.

Each resource carries a provider ID plus a provider-scoped resource ID. File Manager does not assume every resource has a normal filesystem path. This is required for Android document URIs, Linux filesystem resources, GoreeCloud Drive resources, network providers, synchronized-device representations, and future recovery records.

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

A capability is a presentation/execution precondition, not a guarantee of success. The provider remains authoritative and returns typed success, rejection, or failure.

## 4. Android storage architecture

### App-private provider

`LocalFileRepository` is confined to the application's canonical private files root. Requested resources are canonicalized and rejected if they escape that root.

### User-authorized Android document-tree provider

`SafTreeFileRepository` consumes Android Storage Access Framework / `DocumentsContract` resources only after the user selects a tree through the system picker.

File Manager requests persistable read permission and write permission where Android/provider policy permits it. Persisted URI permissions are used to reconstruct previously authorized locations. The selected tree remains bounded by its Android authority plus tree document ID, and child resources are accepted only when they remain within that tree boundary.

Android provider flags are mapped into File Manager capabilities. A selected document tree is not blindly labeled local disk because a DocumentsProvider may represent local, removable, or remote/cloud-backed content.

## 5. Linux client architecture

Linux uses its own provider/discovery/application adapters. It does not reuse Android storage APIs or translate Android URIs into fake filesystem paths.

### 5.1 `:linux-client` — provider, discovery, controller, and CLI layer

`LinuxFileRepository` is the bounded Linux local-filesystem provider. It is rooted at one explicitly selected directory and maps Linux resources into shared provider-scoped identity, capability, result, transfer-verification, and evidence contracts.

Current safety behavior is deliberately conservative:

- the selected root must exist, be a directory, and cannot itself be a symbolic link;
- provider resource IDs are relative to the selected root;
- normalized resource candidates that escape the root are rejected;
- metadata is inspected with `LinkOption.NOFOLLOW_LINKS`;
- symbolic links are visible as `SYMLINK` resources but receive no traversal or mutation capabilities in this slice;
- traversal through symbolic-link components is rejected;
- ordinary reads depend on operating-system readability;
- mutations depend on provider capabilities plus operating-system writability;
- mutation capability is withheld when a resource resolves to a different `FileStore` than the selected root;
- the provider root cannot be renamed or deleted;
- recursive folder deletion is rejected; only empty-folder deletion is implemented;
- recursive folder transfer remains rejected by the shared transfer service;
- ordinary files can participate in the shared SHA-256-verified transfer service.

The selected root is a development authorization scope, not entitlement to crawl or mutate the rest of the host filesystem.

### 5.2 Linux location discovery

`LinuxLocationDiscovery` is a read-only pre-provider metadata layer. It may identify Home, supported XDG user directories, user-facing mount points, and removable-media candidates.

Discovery remains separate from authorization:

- discovery does not construct `LinuxFileRepository`;
- discovery does not create provider identity;
- discovery does not grant read/write access;
- discovery does not traverse candidate contents;
- every candidate requires explicit selection;
- removable-media candidate classification is not proof of physical removability or safe-eject support.

XDG parsing accepts only literal `$HOME` / `${HOME}` forms or explicit absolute paths and does not execute shell expressions. Mount discovery parses `/proc/self/mountinfo`, filters ordinary pseudo/system-only surfaces, and decodes Linux mount escapes.

### 5.3 Linux desktop controller

`LinuxDesktopController` is the application boundary between discovered location metadata and an authorized provider-backed browsing session.

The controller preserves the following invariant by construction:

```text
discover candidate
≠ highlight candidate
≠ authorize provider
```

Only an explicit **Open location** action may invoke the provider factory for the selected candidate. Failed open attempts leave no provider authorized. Folder navigation accepts only folders that belong to the active provider ID. Returning to Locations clears the active provider/current-location state.

The controller is currently read-only from the desktop presentation perspective even though `LinuxFileRepository` contains bounded mutation primitives tested separately.

### 5.4 `:linux-desktop` — development presentation layer

`:linux-desktop` is a separate Compose Multiplatform Desktop presentation module. It depends on `:core` and `:linux-client`; Compose dependencies do not pollute the existing `:linux-client` command-line development distribution.

`LinuxDesktopDevelopmentMain` currently provides:

- edge-integrated location navigation;
- explicit candidate highlighting and **Open location** authorization;
- responsive toolbar behavior;
- solid file-content presentation;
- provider-scoped folder browsing and back navigation;
- symbolic-link visibility without traversal;
- a contextual inspector on wider windows;
- explicit status/error/development-boundary presentation.

This is genuine desktop presentation source, but it remains a development surface. It does not establish rendered/native Glaze conformance, production packaging, complete accessibility/input acceptance, or Linux Stable support.

### 5.5 Command-line development harness

`LinuxDevelopmentMain` remains a separate non-production headless harness. Explicit-root mode performs a read-only provider listing. `--locations` performs read-only location-candidate reporting without creating a provider.

The CLI distribution remains useful as deterministic provider/discovery/build evidence and is intentionally kept separate from the desktop presentation runtime.

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

File Manager must never use path-string substitution to claim one representation is the same resource on another platform. Cross-platform access resolves through provider identity, authorized transfer/synchronization, or bounded GoreeCloud Mesh coordination.

## 7. Operation semantics

Provider mutations use three outcomes:

- `SUCCEEDED` — execution returned success and expected resulting state was obtained where applicable.
- `REJECTED` — File Manager/provider deliberately refused the request.
- `FAILED` — execution or post-operation verification failed.

After a mutation attempt, a user-facing layer must reconcile provider state rather than assuming an error proves no side effect occurred.

The current destructive-operation boundary deliberately refuses recursive folder deletion on Android and Linux development providers. Unified Trash, backup/recovery, Everkeep, operation-journal, and recovery-aware safeguards remain prerequisites for broader destructive workflows.

The current Linux desktop development surface does not expose provider mutation actions.

## 8. Verified ordinary-file transfer

`FileTransferService` lives in `:core` and operates across registered `FileStorageProvider` instances.

For ordinary-file copy/move it:

1. validates the requested filename;
2. checks source `COPY`/`MOVE` and destination `CREATE_FILE` capability;
3. rejects recursive folder transfer;
4. creates the destination through its provider;
5. streams source bytes while computing SHA-256;
6. reopens the resulting destination and computes a second SHA-256 digest;
7. accepts copy success only when the digests match;
8. removes a failed/corrupt destination when possible;
9. for move, deletes the source only after destination integrity succeeds;
10. if source deletion fails after verified copy, retains both resources and reports failure.

## 9. File-name safety

The shared filename policy trims surrounding whitespace and rejects empty names, `.` / `..`, path separators, null characters, and overlong names before mutation.

Providers may impose stricter platform-specific constraints.

## 10. Unified evidence model

File Manager composes separate evidence dimensions for storage/location, synchronization, backup/recovery, Everkeep continuity, sharing/access, Privacy Shield, Wardveil, identity/device context, versions, provenance, and activity.

No dimension may manufacture another. Unknown, unavailable, pending, expired, stale, or unverified evidence remains visibly non-positive.

## 11. Sync versus backup

A synchronized object is replicated working state. Synchronization can propagate deletion or corruption. Independent backup/recovery protection is a separate authority and evidence chain.

```text
sync == synced  ≠  backup == verified recoverable
```

## 12. Platform authority boundaries

- **GoreeCloud Drive** owns its resource/storage/sharing/version identity.
- **GoreeCloud Sync** owns synchronization, replication, and conflict state.
- **GoreeCloud Backup / Everkeep** own backup, continuity, recovery, preservation, and recoverability evidence.
- **Privacy Shield** owns privacy/consent/purpose-limitation authority for privacy-relevant processing.
- **Wardveil Security** owns applicable security evidence and policy outcomes.
- **GoreeCloud Identity** owns authenticated actor/service/device/session and delegated-access authority.
- **GoreeCloud Mesh** coordinates bounded events/state without becoming the underlying authority.
- **Glaze UI** governs presentation, interaction, accessibility, responsiveness, and design semantics but cannot manufacture platform truth.

Operating-system resource access alone does not imply Privacy Shield allowance, Wardveil approval, backup protection, recoverability, or synchronization state.

## 13. Target operation architecture

The complete mutation path remains:

```text
user/app intent
→ resolve exact provider + resource identity
→ obtain applicable identity/privacy/security authority/evidence
→ validate provider capability + operation preconditions
→ enqueue/journal operation
→ provider executes or rejects
→ verify authoritative resulting state
→ reconcile if outcome certainty is lost
→ update activity/provenance and bounded Mesh events
```

Linux location discovery may precede provider authorization, but no operation begins directly from a discovery candidate.

## 14. Search architecture

Search will be layered across local/provider metadata, authorized content indexing, cross-provider aggregation, saved filters/smart collections, and optional natural-language interpretation. Privacy Shield must gate collection/processing scope, and indexes must not become uncontrolled copies of sensitive content.

## 15. UI architecture

Android and Linux share information architecture, terminology, evidence semantics, component roles, and accessibility expectations without sharing inappropriate platform-native layout or storage mechanics.

The Android shell uses Jetpack Compose/Material 3 with GoreeCloud Glaze mapping. The Linux development presentation uses Compose Multiplatform Desktop in the separate `:linux-desktop` module.

The Linux desktop source currently establishes a desktop-specific location sidebar, responsive toolbar, solid content plane, contextual inspector, explicit provider-opening workflow, and read-only navigation. It still requires complete keyboard/pointer/focus behavior, context menus, windows/tabs, drag-and-drop, clipboard workflows, Open With/file associations, native accessibility, mount/removable lifecycle, safe eject, and representative rendered acceptance.

Both platforms require independent current **GLAZE UI V1.3 / 1.3.0** rendered/native acceptance before conformance may be claimed.

## 16. Build, packaging, and acceptance split

Android and Linux are independently validated surfaces.

Android CI checks repository contracts, shared-core tests, Android tests, lint, development APK assembly, package/application identity, and APK artifact evidence.

Linux CI checks repository contracts, Linux-desktop contract validation, shared-core/Linux provider/discovery/controller tests, isolated `:linux-desktop` compilation, the existing `:linux-client` development distribution, explicit-root CLI smoke behavior, and development-only artifact evidence.

The `:linux-client` Gradle tar/distribution is not a supported Debian, Flatpak, AppImage, RPM, Snap, or production package. `:linux-desktop` compilation is not a production desktop package. Linux remains absent from machine-readable supported-platform claims until the independent runtime, packaging, accessibility/input, Glaze, signing/release, and representative-environment gates are satisfied.
