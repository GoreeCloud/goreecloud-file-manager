# GoreeCloud File Manager — Specifications

## Project identity

- **Project:** GoreeCloud File Manager
- **Repository:** `GoreeCloud/goreecloud-file-manager`
- **Development model:** original GoreeCloud-owned native application
- **Current lifecycle:** active Android development / authorized-storage milestone
- **Stable eligibility:** false until required implementation and acceptance gates complete
- **Required native platforms:** Linux and Android
- **Current implemented native client:** Android
- **Linux implementation status:** required target; source/build/runtime acceptance not yet established
- **Production application ID:** `com.goreecloud.filemanager`
- **Development application ID:** `com.goreecloud.filemanager.dev`

## Product responsibility

GoreeCloud File Manager is the primary file-management surface for supported GoreeCloud environments. Its target responsibility includes browsing, organization, operations, search, previews, metadata, storage locations, sharing, synchronization visibility/control, backup and recovery visibility/control, continuity/preservation state, privacy state, security state, identity/access state, device state, provenance, and cross-application file handoff.

Linux and Android are both required first-class native product platforms. This is a product requirement, not an implementation claim: the current repository implementation remains Android-only until Linux source and validation evidence are added.

## Required Linux and Android platform baseline

File Manager must preserve one coherent product and safety model across Linux and Android while adapting storage, permissions, input, lifecycle, packaging, and native system integration to each operating system.

The platform-neutral layer must own provider-scoped resource identity, provider capability semantics, operation outcomes, file-name/conflict policy, verified transfer semantics, operation-journal contracts, file/status/evidence models, GoreeCloud platform-authority adapter contracts, search/provider abstractions, and shared invariants. Platform adapters must not weaken these semantics.

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

The architecture supports storage providers without collapsing their semantics into one filesystem. Provider classes include or target app-private local storage, user-authorized Android document trees, Linux local/mounted filesystems, external/removable storage, GoreeCloud Drive, network/remote locations, synchronized-device representations, and backup/recovery records.

Every provider and visible resource uses provider-scoped identity. The application must not assume that a resource ID is an ordinary local path. Android Storage Access Framework resources use bounded document URIs; Linux local resources may use native path identity through a Linux filesystem adapter; GoreeCloud Drive and network providers retain their own authoritative IDs.

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

## Planned Linux storage baseline

No Linux storage provider is implemented or accepted yet.

The Linux implementation should introduce a native filesystem provider that maps authorized Linux resources into the existing provider-scoped identity and capability model. It must preserve exact resource targeting, bounded operation semantics, safe failure/reconciliation, transfer integrity verification, and independent GoreeCloud authority evidence while adding Linux-specific path, permission, ownership, symlink, mount, removable-media, desktop integration, and network-provider behavior.

The initial Linux milestone should be deliberately bounded to safe local browsing and ordinary-file operations before recursive destructive workflows, broad network behavior, or production claims are enabled.

## Current mutation baseline

Create-folder, rename, delete, and regular-file transfer use typed `SUCCEEDED`, `REJECTED`, and `FAILED` outcomes.

Operations must validate portable/safe names before mutation. Current shared validation rejects empty names, `.` and `..`, path separators, null characters, and names beyond the current 255-character portability limit. Providers may impose additional constraints.

Regular-file copy/move is implemented as a provider-generic service across registered storage providers. The transfer service requires source `COPY` or `MOVE` capability and destination `CREATE_FILE` capability, rejects folder transfer, streams source bytes into the destination, computes SHA-256 over the exact source bytes written, reopens the published destination, computes a second SHA-256 digest, and accepts success only when both digests match. A size match alone is not sufficient integrity evidence. If verification fails, File Manager attempts to remove the destination. A move requests source deletion only after destination integrity has succeeded; if deletion then fails, the verified destination and original are both retained and the operation reports failure rather than fabricating a successful move.

The regular-file transfer service is not yet wired into a user-facing Android destination-selection workflow and has not yet been exercised through a Linux provider. UI-level transfer conflict handling, operation progress/queueing, multi-selection, recursive folder transfer, and unified recovery/Trash behavior remain separate milestones.

After mutation attempts, File Manager refreshes provider state even when execution is uncertain so an error is not treated as proof that no side effect occurred.

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

Current code establishes separate typed state/evidence fields so one positive state cannot silently imply another.

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

## Mandatory platform targets

### Glaze UI

Current required Stable baseline: **GLAZE UI V1.3 / 1.3.0**. Application-specific source mapping, automated checks, rendered/native accessibility, responsive/form-factor acceptance, and representative-platform evidence are required before File Manager can claim current-Stable alignment. The repository's existing UI implementation predates this target and therefore requires explicit migration and fresh consumer acceptance.

### Wardveil Security

Wardveil is the security authority. File Manager must consume normalized Wardveil evidence/contracts rather than scanner-vendor responses. Security coverage and security outcome are separate. Missing coverage is not “clean.”

### Privacy Shield

Privacy Shield is the privacy/consent/data-governance authority. File indexing, previews, search, sharing, synchronization, backup, remote access, and telemetry must remain purpose-limited and minimized.

Android document-tree selection or Linux filesystem access grants resource access through the operating system. Neither automatically establishes Privacy Shield authority for unrelated content processing, indexing, telemetry, sharing, or cross-service transfer.

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
- app-private storage provider plus user-authorized Android document-tree provider
- system `ACTION_OPEN_DOCUMENT_TREE` authorization path
- persisted URI permissions rather than broad storage permission requests
- capability-driven create-folder, rename, and non-recursive delete UI slice
- provider-generic regular-file copy/move service with post-publication SHA-256 destination verification and source-preserving move semantics

## Linux technical direction

The exact Linux language/toolkit/package baseline is not accepted yet. The selected implementation must support native desktop behavior, GoreeCloud's shared File Manager contracts, accessibility, maintainability, packaging/release controls, and representative supported distributions/environments without turning an implementation preference into an unsupported production claim.

Before Linux is listed as a currently supported platform in machine-readable conformance data, the repository must contain the Linux source/build surface and exact-revision evidence required by the platform contract.

## Required documentation

Repository documentation includes README, specifications, features, benefits, competitive objectives, architecture, conformance, and `USER-MANUAL.md`. The corresponding current central user manual must be maintained in `GoreeCloud/User Manuals` and material project state must remain reconciled with the canonical Google Drive project specification and changelog.

## Acceptance boundary

Source/build acceptance requires the exact revision to pass repository validation and all applicable platform-specific validation for the source present in that revision.

The current repository CI proves only its implemented Android checks. It does not establish Linux source/build acceptance, production acceptance, complete storage-provider acceptance, user-facing copy/move/Trash/recovery acceptance, recursive transfer acceptance, current GLAZE UI V1.3 consumer acceptance, Wardveil/Privacy Shield/Everkeep runtime acceptance, Identity/Mesh production integration, production signing/deployment, or Stable qualification.
