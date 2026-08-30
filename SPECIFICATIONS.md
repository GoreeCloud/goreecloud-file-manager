# GoreeCloud File Manager — Specifications

## Project identity

- **Project:** GoreeCloud File Manager
- **Repository:** `GoreeCloud/goreecloud-file-manager`
- **Development model:** original GoreeCloud-owned native application
- **Current lifecycle:** active development / foundation milestone
- **Stable eligibility:** false until required implementation and acceptance gates complete
- **Initial native client:** Android
- **Production application ID:** `com.goreecloud.filemanager`
- **Development application ID:** `com.goreecloud.filemanager.dev`

## Product responsibility

GoreeCloud File Manager is the primary file-management surface for supported GoreeCloud environments. Its target responsibility includes browsing, organization, operations, search, previews, metadata, storage locations, sharing, synchronization visibility/control, backup and recovery visibility/control, continuity/preservation state, privacy state, security state, identity/access state, device state, provenance, and cross-application file handoff.

## Storage and location model

The architecture must support storage providers without collapsing their semantics into one filesystem. Planned provider classes include local device storage, user-authorized Android document roots, external/removable storage, GoreeCloud Drive, network/remote locations, synchronized-device representations, and backup/recovery records.

A provider must expose only operations it can actually authorize and verify. A file location, synchronized copy, historical version, backup copy, recovery point, and preservation record are distinct states.

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
9. Destructive operations require exact target identity, current authority, clear scope, and safe failure behavior.
10. File operations should be resumable, idempotent, or reconcilable where the underlying provider supports it.

## Mandatory platform targets

### Glaze UI

Current required Stable baseline: **2.0.0**. Application-specific source mapping, automated checks, rendered/native accessibility, responsive/form-factor acceptance, and representative-device evidence are required before File Manager can claim current-Stable alignment.

### Wardveil Security

Wardveil is the security authority. File Manager must consume normalized Wardveil evidence/contracts rather than scanner-vendor responses. Security coverage and security outcome are separate. Missing coverage is not “clean.”

### Privacy Shield

Privacy Shield is the privacy/consent/data-governance authority. File indexing, previews, search, sharing, synchronization, backup, remote access, and telemetry must remain purpose-limited and minimized.

### Everkeep

Everkeep is the continuity/resilience/preservation authority. File Manager may expose backup, recovery, verification, preservation, and portability state, but it must preserve the distinction between a backup existing and a resource being verified recoverable.

### GoreeCloud Identity

Identity owns authenticated actor/service identity, account boundaries, devices, credentials, sessions, authorization primitives, and delegated authority relevant to file access.

### GoreeCloud Mesh

Mesh coordinates bounded file/service/device events and cross-application state without transferring the independent authority of Drive, Sync, Identity, Privacy Shield, Wardveil, or Everkeep.

## Android foundation baseline

- `compileSdk = 37`
- `targetSdk = 36`
- `minSdk = 26`
- Java 17
- Jetpack Compose with the current GoreeCloud Android dependency baseline
- app-private local storage is the only concrete provider in the first foundation revision
- broad filesystem/storage permissions are intentionally not requested yet

## Acceptance boundary

The current foundation milestone can be considered source implemented only when repository validation, unit tests, lint, and Android assembly pass for the exact revision. It is not production acceptance, full File Manager feature acceptance, Glaze UI consumer acceptance, or platform-integration acceptance.
