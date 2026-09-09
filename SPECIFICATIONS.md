# GoreeCloud File Manager — Specifications

## Project identity

- **Project:** GoreeCloud File Manager
- **Repository:** `GoreeCloud/goreecloud-file-manager`
- **Development model:** original GoreeCloud-owned native application
- **Current lifecycle:** active Android development / shared-core extraction / bounded Linux provider development
- **Stable eligibility:** false until required implementation and acceptance gates complete
- **Required native platforms:** Linux and Android
- **Current implemented native user-facing client:** Android
- **Linux implementation status:** shared JVM core plus bounded local-filesystem provider and non-production development harness are present; exact-head source/build validation, desktop UI, production packaging, supported-runtime acceptance, and Stable acceptance remain separate gates
- **Production application ID:** `com.goreecloud.filemanager`
- **Development application ID:** `com.goreecloud.filemanager.dev`

## Product responsibility

GoreeCloud File Manager is the primary file-management surface for supported GoreeCloud environments. Its target responsibility includes browsing, organization, operations, search, previews, metadata, storage locations, sharing, synchronization visibility/control, backup and recovery visibility/control, continuity/preservation state, privacy state, security state, identity/access state, device state, provenance, and cross-application file handoff.

Linux and Android are both required first-class native product platforms. This is a product requirement, not a blanket implementation claim. Android is the current native user-facing application. The current Linux source establishes a development provider/application boundary and build harness, but not the accepted desktop experience or supported production platform.

## Required Linux and Android platform baseline

File Manager must preserve one coherent product and safety model across Linux and Android while adapting storage, permissions, input, lifecycle, packaging, and native system integration to each operating system.

The platform-neutral layer owns provider-scoped resource identity, provider capability semantics, operation outcomes, file-name/conflict policy, verified transfer semantics, operation-journal contracts, file/status/evidence models, GoreeCloud platform-authority adapter contracts, search/provider abstractions, and shared invariants. Platform adapters must not weaken these semantics.

The current repository implements this separation through the JVM `:core` module, consumed by the Android application and the Linux development module. Shared code currently includes the typed resource/status model, provider contract, file-name policy, platform-authority adapter boundaries, and SHA-256-verified ordinary-file transfer service. Moving a contract into `:core` establishes code sharing, not production acceptance of either platform.

### Android requirements

Android must continue to use least-privilege operating-system storage mechanisms: app-private storage, Storage Access Framework/document providers, persisted user grants, scoped-storage behavior, and supported system integrations. Android behavior must account for document/content URI identity, touch/gesture input, phone/tablet/foldable layouts, system back behavior, runtime permission state, share/Open/Save flows, and application lifecycle behavior.

Android must not request broad unrestricted filesystem authority merely to imitate Linux path access. Any exceptional permission model requires separate governance, justification, and acceptance.

### Linux requirements

The Linux client must provide a native desktop file-management experience over authorized filesystem and provider resources. The Linux platform layer targets:

- ordinary filesystem paths where authorized;
- XDG user directories;
- mounted filesystems and removable media;
- Unix permissions, ownership, and related metadata;
- symbolic-link visibility and safe traversal behavior;
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

A platform-specific Development or preview build may be truthfully scoped to one platform, but normal File Manager product direction remains first-class Linux and Android support.

## Storage and location model

The architecture supports storage providers without collapsing their semantics into one filesystem. Provider classes include app-private local storage, user-authorized Android document trees, bounded Linux local filesystems, and target external/removable storage, GoreeCloud Drive, network/remote locations, synchronized-device representations, and backup/recovery records.

Every provider and visible resource uses provider-scoped identity. The application must not assume that a resource ID is an ordinary local path. Android Storage Access Framework resources use bounded document URIs; the current Linux development provider uses provider-relative path identity rooted at one selected directory; GoreeCloud Drive and network providers retain their own authoritative IDs.

A provider must expose only operations it can actually authorize and support. Per-resource capability concepts are `READ`, `LIST_CHILDREN`, `CREATE_FILE`, `CREATE_FOLDER`, `RENAME`, `DELETE`, `COPY`, and `MOVE`.

A file location, synchronized copy, historical version, backup copy, recovery point, and preservation record are distinct states.

## Current Android storage baseline

### App-private provider

The built-in local provider is confined to the application's canonical private files root. Requests that canonicalize outside that root are rejected.

Current implemented provider operations include list, create file, create folder, rename, delete, read, and write primitives as applicable. Recursive folder deletion is rejected.

### User-authorized Android document trees

Broader Android storage access uses the operating-system document-tree picker and persisted URI permissions. File Manager does not request unrestricted filesystem access for this workflow.

The selected tree is bounded by Android authority plus tree document ID. File Manager reconstructs accessible providers from Android persisted URI-permission state and distinguishes read-only from read/write authorization.

Android document-provider flags are mapped into File Manager resource capabilities. Mutation UI must not be shown merely because File Manager has a write grant; the item/provider must also advertise the applicable capability.

A selected document tree is not automatically classified as ordinary local disk because Android DocumentsProviders can represent local, removable, or remote/cloud-backed content.

## Current Linux development storage baseline

The repository now contains `linux-client/src/main/kotlin/com/goreecloud/filemanager/linux/LinuxFileRepository.kt`, a deliberately bounded local-filesystem provider used by the non-production Linux development module.

The current provider contract is narrower than the target desktop client:

- construction requires one explicit existing directory as the provider root;
- the root is normalized and resolved to a real directory and cannot itself be a symbolic link;
- provider resource IDs are relative to that root rather than globally reusable path strings;
- normalized resources that would escape the selected root are rejected;
- metadata is read with `NOFOLLOW_LINKS`;
- symbolic links are represented as `SYMLINK` resources but receive no traversal or mutation capability in this slice;
- filesystem reads are allowed only where operating-system permissions permit them;
- create, rename, write, move-source deletion, and delete capabilities are withheld across a detected `FileStore`/mount boundary until mount-specific mutation policy is implemented and accepted;
- file and directory mutation requires writable operating-system state and provider capability;
- provider-root mutation is rejected;
- recursive directory deletion is rejected; only empty directory deletion is implemented;
- recursive folder copy/move remains rejected by the shared transfer service;
- ordinary files can participate in the shared SHA-256-verified transfer service.

This provider is a development foundation, not an authorization statement for the whole Linux filesystem. XDG discovery, automatic mount/removable-media discovery, ownership/ACL management, file associations/Open With, desktop Trash, drag-and-drop, clipboard integration, network locations, safe eject, desktop accessibility, and the native desktop Glaze UI remain separate milestones.

`LinuxDevelopmentMain.kt` is a non-production command-line harness. It accepts exactly one explicit root and exposes read-only listing through the provider. It exists to exercise the Linux build/provider boundary before a production desktop toolkit and package format are accepted.

## Current mutation baseline

Create-folder, rename, delete, and regular-file transfer use typed `SUCCEEDED`, `REJECTED`, and `FAILED` outcomes.

Operations validate portable/safe names before mutation. Current shared validation rejects empty names, `.` and `..`, path separators, null characters, and names beyond the current 255-character portability limit. Providers may impose additional constraints.

Regular-file copy/move is implemented as a provider-generic service across registered storage providers. The transfer service requires source `COPY` or `MOVE` capability and destination `CREATE_FILE` capability, rejects folder transfer, streams source bytes into the destination, computes SHA-256 over the exact source bytes written, reopens the published destination, computes a second SHA-256 digest, and accepts success only when both digests match. A size match alone is not sufficient integrity evidence. If verification fails, File Manager attempts to remove the destination. A move requests source deletion only after destination integrity has succeeded; if deletion then fails, the verified destination and original are both retained and the operation reports failure rather than fabricating a successful move.

The shared test suite covers cross-provider corruption detection and source preservation. Linux-provider tests are also present for Linux-to-Linux copy/move through this same service; their authoritative acceptance status is determined by exact-head Linux workflow results, not by test source merely existing.

The regular-file transfer service is not yet wired into a user-facing Android destination-selection workflow or a Linux desktop UI. UI-level transfer conflict handling, operation progress/queueing, multi-selection, recursive folder transfer, and unified recovery/Trash behavior remain separate milestones.

After mutation attempts, File Manager refreshes provider state where the user-facing platform layer can observe the provider, even when execution is uncertain, so an error is not treated as proof that no side effect occurred.

Recursive directory deletion is deliberately disabled until the application implements and accepts the required Trash, recovery, backup/Everkeep, durable operation, and destructive-action safeguards. Recursive folder transfer is likewise not enabled by the current transfer service.

## Unified status model

A supported file can eventually expose bounded state for:

- storage and local availability;
- synchronization;
- backup and verified recoverability;
- Everkeep continuity/preservation;
- sharing and ownership;
- permissions/access;
- Privacy Shield privacy state;
- Wardveil security state;
- versions and provenance;
- device availability and recent activity.

Current shared code establishes separate typed state/evidence fields so one positive state cannot silently imply another.

## Core invariants

1. Synchronization never implies backup or recoverability.
2. Backup existence never implies verified recoverability.
3. Wardveil security state is authoritative only when current verified Wardveil evidence exists.
4. Privacy state is authoritative only when current Privacy Shield evidence exists.
5. Glaze UI may present platform state but may not manufacture or upgrade authority truth.
6. GoreeCloud Mesh coordinates and transports bounded state; it does not become the underlying security, privacy, continuity, or identity authority.
7. GoreeCloud Identity establishes identity/access authority and does not automatically broaden Privacy Shield or Wardveil authority.
8. Unknown, unavailable, stale, expired, unsupported, or unverified evidence must remain visibly non-positive.
9. Destructive operations require exact provider/resource identity, clear scope, capability/authorization checks, and safe failure behavior.
10. An operation error cannot automatically be treated as proof that no side effect occurred; reconciliation may be required.
11. Provider capability and GoreeCloud platform authority are separate concepts. A filesystem provider reporting delete support does not establish backup, privacy, or Wardveil approval.
12. Matching file size is not proof of identical transfer content; successful regular-file transfer requires verified source/destination content integrity.
13. A move must not delete its source until the destination has passed integrity verification.
14. Platform-native resource identifiers must remain provider-scoped and must not be treated as universally interchangeable across Linux, Android, Drive, network, synchronization, backup, or recovery contexts.
15. Platform acceptance is independent: successful Android validation cannot be reused as Linux acceptance, and vice versa.
16. Linux symbolic links must not silently widen provider scope. The current development provider exposes them as non-traversable/non-mutable resources until a separately reviewed policy exists.
17. A mount boundary must not silently widen destructive authority. The current Linux provider withholds mutations when the resource's `FileStore` differs from the selected root's store.
18. Development distribution output is not equivalent to an accepted Linux package or supported-platform declaration.

## Mandatory platform targets

### Glaze UI

Current required Stable baseline: **GLAZE UI V1.3 / 1.3.0**. Application-specific source mapping, automated checks, rendered/native accessibility, responsive/form-factor acceptance, and representative-platform evidence are required before File Manager can claim current-Stable alignment. The Android UI implementation predates this target and therefore requires explicit migration and fresh consumer acceptance. The Linux development harness is not a Glaze UI desktop implementation.

### Wardveil Security

Wardveil is the security authority. File Manager must consume normalized Wardveil evidence/contracts rather than scanner-vendor responses. Security coverage and security outcome are separate. Missing coverage is not “clean.”

### Privacy Shield

Privacy Shield is the privacy/consent/data-governance authority. File indexing, previews, search, sharing, synchronization, backup, remote access, and telemetry must remain purpose-limited and minimized.

Android document-tree selection or Linux filesystem access grants resource access through the operating system/provider boundary. Neither automatically establishes Privacy Shield authority for unrelated content processing, indexing, telemetry, sharing, or cross-service transfer.

### Everkeep

Everkeep is the continuity/resilience/preservation authority. File Manager may expose backup, recovery, verification, preservation, and portability state, but it must preserve the distinction between a backup existing and a resource being verified recoverable.

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
- `:app` depends on the shared JVM `:core` module
- app-private storage provider plus user-authorized Android document-tree provider
- system `ACTION_OPEN_DOCUMENT_TREE` authorization path
- persisted URI permissions rather than broad storage permission requests
- capability-driven create-folder, rename, and non-recursive delete UI slice
- provider-generic regular-file copy/move service with post-publication SHA-256 destination verification and source-preserving move semantics

## Linux technical baseline and direction

The current development source uses Kotlin/JVM 17 for `:linux-client` and consumes `:core`. The Gradle `application` plugin produces a JVM development distribution and start script for the explicit-root harness. This establishes a testable source/build boundary only; it does not choose or accept the production desktop UI toolkit.

The exact supported distribution set and production package format remain unaccepted. A generated Gradle tar distribution is build evidence, not a supported Debian, Flatpak, AppImage, RPM, Snap, or other package.

Before Linux is listed as a currently supported platform in machine-readable conformance data, the exact Linux candidate revision must have the required source/build tests plus accepted desktop behavior, packaging/release, accessibility/input, provider/runtime, security/privacy/continuity, and other governing evidence appropriate to the claimed scope.

## CI and evidence boundary

The repository defines independent exact-source workflows:

- **Android foundation validation:** repository contract validation, shared-core tests, Android unit tests, Android lint, development APK assembly, APK identity verification, and artifact publication.
- **Linux development validation:** repository contract validation, shared-core tests, Linux-provider tests, JVM development distribution build, explicit-root harness smoke test, distribution digest, and development-boundary evidence.
- **Platform Contract:** shared GoreeCloud manifest/conformance evaluation against the immutable central validator revision pinned by the repository workflow.

Only completed workflow results for the exact candidate head count as that candidate's CI evidence. A prior green head does not validate a later revision.

## Required documentation

Repository documentation includes README, specifications, features, benefits, competitive objectives, architecture, conformance, and `USER-MANUAL.md`. The corresponding current central user manual must be maintained in `GoreeCloud/User Manuals` and material project state must remain reconciled with the canonical Google Drive project specification and changelog.

## Acceptance boundary

Source/build acceptance requires the exact revision to pass repository validation and all applicable platform-specific validation for the source present in that revision.

Even successful Android and Linux development workflows do not establish production acceptance, complete storage-provider acceptance, user-facing copy/move/Trash/recovery acceptance, recursive transfer acceptance, current GLAZE UI V1.3 consumer acceptance, Wardveil/Privacy Shield/Everkeep runtime acceptance, Identity/Mesh production integration, Linux desktop UI/package acceptance, production signing/deployment, or Stable qualification.
