# GoreeCloud File Manager — Specifications

## Project identity

- **Project:** GoreeCloud File Manager
- **Repository:** `GoreeCloud/goreecloud-file-manager`
- **Development model:** original GoreeCloud-owned native application
- **Current lifecycle:** active Android development / authorized-storage milestone
- **Stable eligibility:** false until required implementation and acceptance gates complete
- **Initial native client:** Android
- **Production application ID:** `com.goreecloud.filemanager`
- **Development application ID:** `com.goreecloud.filemanager.dev`

## Product responsibility

GoreeCloud File Manager is the primary file-management surface for supported GoreeCloud environments. Its target responsibility includes browsing, organization, operations, search, previews, metadata, storage locations, sharing, synchronization visibility/control, backup and recovery visibility/control, continuity/preservation state, privacy state, security state, identity/access state, device state, provenance, and cross-application file handoff.

## Storage and location model

The architecture supports storage providers without collapsing their semantics into one filesystem. Provider classes include or target app-private local storage, user-authorized Android document trees, external/removable storage, GoreeCloud Drive, network/remote locations, synchronized-device representations, and backup/recovery records.

Every provider and visible resource uses provider-scoped identity. The application must not assume that a resource ID is an ordinary local path. Android Storage Access Framework resources use bounded document URIs, while future GoreeCloud Drive and network providers will retain their own authoritative IDs.

A provider must expose only operations it can actually authorize and support. Per-resource capability concepts are `READ`, `LIST_CHILDREN`, `CREATE_FILE`, `CREATE_FOLDER`, `RENAME`, `DELETE`, `COPY`, and `MOVE`.

A file location, synchronized copy, historical version, backup copy, recovery point, and preservation record are distinct states.

## Current Android storage baseline

### App-private provider

The built-in local provider is confined to the application's canonical private files root. Requests that canonicalize outside that root are rejected.

Current implemented operations are list, create folder, rename, and delete. Recursive folder deletion is rejected.

### User-authorized Android document trees

Broader Android storage access uses the operating-system document-tree picker and persisted URI permissions. File Manager does not request unrestricted filesystem access for this workflow.

The selected tree is bounded by Android authority plus tree document ID. File Manager reconstructs accessible providers from Android persisted URI-permission state and distinguishes read-only from read/write authorization.

Android document-provider flags are mapped into File Manager resource capabilities. Mutation UI must not be shown merely because File Manager has a write grant; the item/provider must also advertise the applicable capability.

A selected document tree is not automatically classified as ordinary local disk because Android DocumentsProviders can represent local, removable, or remote/cloud-backed content.

## Current mutation baseline

Create-folder, rename, and delete use typed `SUCCEEDED`, `REJECTED`, and `FAILED` outcomes.

Operations must validate portable/safe names before mutation. Current shared validation rejects empty names, `.` and `..`, path separators, null characters, and names beyond the current 255-character portability limit. Providers may impose additional constraints.

After mutation attempts, File Manager refreshes provider state even when execution is uncertain so an error is not treated as proof that no side effect occurred.

Recursive directory deletion is deliberately disabled until the application implements and accepts the required Trash, recovery, backup/Everkeep, durable operation, and destructive-action safeguards.

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

## Mandatory platform targets

### Glaze UI

Current required Stable baseline: **2.0.0**. Application-specific source mapping, automated checks, rendered/native accessibility, responsive/form-factor acceptance, and representative-device evidence are required before File Manager can claim current-Stable alignment.

### Wardveil Security

Wardveil is the security authority. File Manager must consume normalized Wardveil evidence/contracts rather than scanner-vendor responses. Security coverage and security outcome are separate. Missing coverage is not “clean.”

### Privacy Shield

Privacy Shield is the privacy/consent/data-governance authority. File indexing, previews, search, sharing, synchronization, backup, remote access, and telemetry must remain purpose-limited and minimized.

Android document-tree selection grants resource access through Android. It does not by itself establish Privacy Shield authority for unrelated content processing, indexing, telemetry, sharing, or cross-service transfer.

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
- capability-driven create-folder, rename, and non-recursive delete development slice

## Required documentation

Repository documentation includes README, specifications, features, benefits, competitive objectives, architecture, conformance, and `USER-MANUAL.md`. The corresponding current central user manual must be maintained in `GoreeCloud/User Manuals` and material project state must remain reconciled with the canonical Google Drive project specification and changelog.

## Acceptance boundary

Source/build acceptance requires the exact revision to pass repository validation, unit tests, lint, Android assembly, package/application identity checks, and artifact publication.

Passing those checks does not establish production acceptance, complete storage-provider acceptance, safe copy/move/Trash/recovery acceptance, Glaze UI consumer acceptance, Wardveil/Privacy Shield/Everkeep runtime acceptance, Identity/Mesh production integration, production signing/deployment, or Stable qualification.
