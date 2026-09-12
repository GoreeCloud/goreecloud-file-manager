# GoreeCloud File Manager — Specifications

## Project identity

- **Project:** GoreeCloud File Manager
- **Repository:** `GoreeCloud/goreecloud-file-manager`
- **Development model:** original GoreeCloud-owned native application
- **Current lifecycle:** active Android development / shared-core foundation / bounded Linux provider-discovery-controller development / Linux desktop presentation development
- **Stable eligibility:** false until required implementation and acceptance gates complete
- **Required native platforms:** Linux and Android
- **Current production-shaped native user-facing development client:** Android
- **Linux implementation status:** shared JVM core, bounded local-filesystem provider, read-only Home/XDG/mount discovery, explicit-open desktop controller, command-line development harness, and a separate graphical Compose Desktop development surface are present; production packaging, representative runtime acceptance, complete accessibility/input acceptance, current Glaze acceptance, supported-platform declaration, signing/release controls, and Stable acceptance remain separate gates
- **Production application ID:** `com.goreecloud.filemanager`
- **Development application ID:** `com.goreecloud.filemanager.dev`

## Product responsibility

GoreeCloud File Manager is the primary file-management surface for supported GoreeCloud environments. Its target responsibility includes browsing, organization, operations, search, previews, metadata, storage locations, sharing, synchronization visibility/control, backup and recovery visibility/control, continuity/preservation state, privacy state, security state, identity/access state, device state, provenance, and cross-application file handoff.

Linux and Android are both required first-class native product platforms. This is a product requirement, not a blanket implementation claim. Android is the current production-shaped native user-facing development application. Linux now has real graphical desktop development source in addition to the bounded provider/discovery foundation, but it is not yet an accepted supported production platform.

## Required Linux and Android platform baseline

File Manager must preserve one coherent product and safety model across Linux and Android while adapting storage, permissions, input, lifecycle, packaging, and native system integration to each operating system.

The platform-neutral layer owns provider-scoped resource identity, provider capability semantics, operation outcomes, filename/conflict policy, verified transfer semantics, status/evidence models, GoreeCloud authority adapter contracts, and shared invariants. Platform adapters must not weaken these semantics.

The repository implements this separation through JVM `:core`, consumed by Android and the Linux development layers. Moving a contract into `:core` establishes code sharing, not production acceptance of either platform.

### Android requirements

Android must continue to use least-privilege storage mechanisms: app-private storage, Storage Access Framework/document providers, persisted user grants, scoped-storage behavior, and supported system integrations. Android behavior must account for document/content URI identity, touch/gesture input, adaptive phone/tablet/foldable layouts, system back behavior, runtime permission state, share/Open/Save flows, and lifecycle behavior.

Android must not request broad unrestricted filesystem authority merely to imitate Linux path access.

### Linux requirements

The Linux client must provide a desktop file-management experience over explicitly authorized filesystem and provider resources. The Linux platform layer targets:

- ordinary filesystem paths where authorized;
- XDG user directories;
- mounted filesystems and removable media;
- Unix permissions, ownership, and related metadata;
- symbolic-link visibility and reviewed traversal behavior;
- file associations and Open With handling;
- clipboard file operations and drag-and-drop;
- full keyboard and pointer workflows;
- windows, tabs, split/dual-pane desktop patterns, and desktop-appropriate density;
- mount, disconnect, reconnect, and safe-removal state;
- supported network filesystems/providers and remote locations;
- native desktop accessibility and system-integration behavior.

Linux path traversal, symbolic-link escape, mount-boundary, permission, ownership, race, and destructive-operation behavior must be handled explicitly. Linux must not inherit Android document-URI assumptions.

### Cross-platform resource identity

A Linux filesystem path, Android content/document URI, GoreeCloud Drive resource ID, synchronized-device identity, backup/recovery record, and remote-provider identifier remain distinct provider-scoped identities. File Manager must not treat a platform-native path or URI string as a universal resource identifier.

Cross-platform handoff must resolve resources through provider-aware identity, authorized synchronization/transfer mechanisms, or GoreeCloud Mesh coordination rather than path-string substitution.

### Platform-specific acceptance

Linux and Android require independent source/build provenance, packaging, storage/provider validation, accessibility/input validation, performance evidence, signing/release controls, and rollback/recovery evidence. Android evidence does not establish Linux acceptance, and Linux evidence does not establish Android acceptance.

## Storage and location model

The architecture supports storage providers without collapsing their semantics into one filesystem. Provider classes include app-private local storage, user-authorized Android document trees, bounded Linux local filesystems, and target external/removable storage, GoreeCloud Drive, network/remote locations, synchronized-device representations, and backup/recovery records.

Every provider and visible resource uses provider-scoped identity. The application must not assume that a resource ID is an ordinary local path.

Linux location discovery is a separate pre-provider metadata layer. A discovered Home/XDG/mount path is a candidate for user presentation and explicit selection, not a provider resource and not authorization to read or mutate it.

A provider must expose only operations it can actually authorize and support. Current per-resource capability concepts are `READ`, `LIST_CHILDREN`, `CREATE_FILE`, `CREATE_FOLDER`, `RENAME`, `DELETE`, `COPY`, and `MOVE`.

A file location, synchronized copy, historical version, backup copy, recovery point, and preservation record are distinct states.

## Current Android storage baseline

### App-private provider

The built-in local provider is confined to the application's canonical private files root. Requests that canonicalize outside that root are rejected.

Current implemented provider operations include list, create file, create folder, rename, delete, read, and write primitives as applicable. Recursive folder deletion is rejected.

### User-authorized Android document trees

Broader Android storage access uses the operating-system document-tree picker and persisted URI permissions. File Manager does not request unrestricted filesystem access for this workflow.

The selected tree is bounded by Android authority plus tree document ID. File Manager reconstructs accessible providers from Android persisted URI-permission state and distinguishes read-only from read/write authorization.

Android provider flags are mapped into File Manager resource capabilities. A selected document tree is not automatically classified as ordinary local disk because Android DocumentsProviders can represent local, removable, or remote/cloud-backed content.

## Current Linux development storage baseline

The bounded Linux storage and application layer is implemented in `:linux-client`.

`LinuxFileRepository` requires one explicit existing directory as the provider root and enforces:

- normalized root confinement;
- provider-relative resource IDs;
- path-escape rejection;
- `NOFOLLOW_LINKS` metadata reads;
- symbolic-link visibility without traversal/mutation in the current slice;
- operating-system readability/writability checks;
- mutation withholding across a detected `FileStore` boundary;
- provider-root mutation rejection;
- non-recursive folder deletion;
- non-recursive folder transfer;
- participation in the shared SHA-256-verified ordinary-file transfer service.

This provider is a development authorization boundary, not authority over the whole Linux filesystem.

## Current Linux location-discovery baseline

`LinuxLocationDiscovery` provides read-only pre-provider candidate discovery.

Current behavior includes:

- Home when it exists as a non-symlink directory;
- recognized XDG user directories from `user-dirs.dirs`;
- literal `$HOME` / `${HOME}` expansion and explicit absolute paths only;
- rejection of relative and shell-expression values;
- `/proc/self/mountinfo` parsing with mount escape decoding;
- filtering of ordinary pseudo/system-only mount surfaces;
- `/media` and `/run/media` classification as removable-media candidates rather than proof of removability/ejectability;
- duplicate normalized path collapse;
- `requiresExplicitSelection = true` for every candidate.

Discovery does not construct `LinuxFileRepository`, grant file access, traverse candidate content, or authorize mutation.

## Current Linux desktop development baseline

The repository contains a separate `:linux-desktop` presentation module using Compose Multiplatform Desktop `1.12.0`. It depends on `:core` and `:linux-client` while keeping desktop UI dependencies out of the existing CLI development distribution.

`LinuxDesktopController` provides the explicit authorization boundary between discovery metadata and a provider-backed browsing session:

```text
discover candidate
≠ highlight candidate
≠ authorize provider
```

Only the explicit **Open location** action may construct the bounded provider for the selected candidate. Failed open attempts leave no provider authorized. Folder navigation is provider-ID scoped. Returning to Locations clears active provider/current-location state.

`LinuxDesktopDevelopmentMain` currently provides:

- edge-integrated Linux location navigation;
- explicit selection/open workflow;
- responsive desktop toolbar;
- solid file-content plane;
- provider-scoped read-only folder browsing;
- symbolic-link visibility without traversal;
- contextual inspector on wider windows;
- explicit status/error/development-boundary presentation.

The graphical surface intentionally does **not** expose provider mutation primitives in this milestone.

This source is a desktop presentation development foundation. It does not establish supported Linux packaging, representative desktop/runtime compatibility, complete keyboard/pointer/focus behavior, assistive-technology acceptance, reduced-transparency/contrast/motion acceptance, safe eject, production signing, or Stable qualification.

## Current Linux command-line development harness

`LinuxDevelopmentMain` remains a non-production headless engineering surface.

- Explicit-root mode performs a read-only listing through `LinuxFileRepository`.
- `--locations` reports read-only location candidates and does not construct a provider.

The `:linux-client` Gradle distribution is development evidence only and remains separate from the graphical desktop module.

## Current mutation baseline

Create-folder, rename, delete, and regular-file transfer use typed `SUCCEEDED`, `REJECTED`, and `FAILED` outcomes.

Current shared filename validation rejects empty names, `.` and `..`, path separators, null characters, and names beyond the current portability limit. Providers may impose additional constraints.

Regular-file copy/move is implemented as a provider-generic service. It validates capabilities, streams source bytes, computes SHA-256, reopens the destination, computes a second digest, and accepts success only when the digests match. Move removes the source only after destination verification. If source deletion then fails, both resources remain and the operation reports failure.

The service is not yet connected to complete user-facing destination-selection workflows on Android or Linux desktop. Recursive folder deletion and transfer remain disabled pending Trash/recovery/continuity safeguards.

## Unified status model

A supported file can eventually expose bounded state for storage/local availability, synchronization, backup and verified recoverability, Everkeep continuity/preservation, sharing/ownership, permissions/access, Privacy Shield, Wardveil Security, versions/provenance, device availability, and recent activity.

Current shared code keeps these states independent. Unknown, unavailable, stale, expired, unsupported, or unverified evidence must remain visibly non-positive.

## Core invariants

1. Synchronization never implies backup or recoverability.
2. Backup existence never implies verified recoverability.
3. Wardveil security state is authoritative only with current applicable Wardveil evidence.
4. Privacy state is authoritative only with current applicable Privacy Shield evidence.
5. Glaze UI may present platform state but may not manufacture authority truth.
6. GoreeCloud Mesh coordinates bounded state; it does not become the underlying authority.
7. GoreeCloud Identity does not automatically broaden Privacy Shield or Wardveil authority.
8. Destructive operations require exact provider/resource identity, clear scope, capability/authorization checks, and safe failure behavior.
9. Matching size is not proof of transfer integrity; ordinary-file transfer requires verified content integrity.
10. A move must not delete its source until the destination passes integrity verification.
11. Linux symbolic links must not silently widen provider scope.
12. Mount boundaries must not silently widen destructive authority.
13. Development distribution or desktop source compilation is not supported-platform acceptance.
14. Linux location discovery is not filesystem authorization.
15. A removable-media candidate label is not proof of removability, ejectability, or safe-removal acceptance.
16. Android and Linux acceptance evidence is independent.

## Mandatory platform targets

### Glaze UI

Current required Stable baseline: **GLAZE UI V1.3 / 1.3.0**. Application-specific source mapping, automated checks, rendered/native accessibility, responsive/form-factor acceptance, input behavior, state presentation, and representative-platform evidence are required before File Manager can claim current-Stable alignment.

The Linux desktop development source targets this governed direction but is not conformance evidence by itself. Android also requires fresh current-revision acceptance.

### Wardveil Security

Wardveil is the security authority. File Manager consumes normalized Wardveil evidence/contracts and must not convert missing coverage into a clean/protected claim.

### Privacy Shield

Privacy Shield is the privacy/consent/data-governance authority. File indexing, previews, search, sharing, synchronization, backup, remote access, and telemetry must remain purpose-limited and minimized.

Operating-system resource access does not automatically establish Privacy Shield authorization for unrelated processing.

### Everkeep

Everkeep is the continuity/resilience/preservation authority. Backup existence and verified recoverability remain distinct.

### GoreeCloud Identity

Identity owns authenticated actor/service identity, account boundaries, devices, credentials, sessions, authorization primitives, and delegated authority relevant to file access.

### GoreeCloud Mesh

Mesh coordinates bounded file/service/device events and cross-application state without transferring the independent authority of Drive, Sync, Identity, Privacy Shield, Wardveil, or Everkeep.

## Android technical baseline

- `compileSdk = 37`
- `targetSdk = 36`
- `minSdk = 26`
- Java 17
- Jetpack Compose with the current GoreeCloud Android dependency baseline
- `:app` depends on shared JVM `:core`
- app-private provider plus user-authorized Android document-tree provider
- system `ACTION_OPEN_DOCUMENT_TREE` authorization
- persisted URI permissions rather than broad storage permission requests
- capability-driven bounded mutation UI
- provider-generic regular-file transfer service with post-publication SHA-256 verification

## Linux technical baseline and direction

- Kotlin/JVM 17 for `:linux-client` and `:linux-desktop`.
- `:linux-client` depends on `:core` and owns Linux provider/discovery/controller/CLI development behavior.
- `:linux-desktop` depends on `:core` and `:linux-client` and uses Compose Multiplatform Desktop `1.12.0`.
- The existing `:linux-client` Gradle application distribution remains a CLI development artifact.
- Linux CI separately validates repository contracts, desktop contracts, shared/Linux tests, isolated desktop compilation, CLI distribution construction, and smoke evidence.
- Exact supported distributions and production package formats remain unaccepted.
- Linux remains absent from machine-readable supported-platform claims until native runtime, packaging, accessibility/input, Glaze, signing/release, and representative-environment acceptance are complete.
