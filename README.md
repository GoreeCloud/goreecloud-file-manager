# GoreeCloud File Manager

GoreeCloud File Manager is the original GoreeCloud-owned file-management application for browsing and controlling files across supported local, cloud, synchronized, removable, network, backup, and continuity contexts.

> **Development status:** native Android foundation in active development. This repository is **not Stable or production accepted**. The current implementation is an initial native shell and evidence-safe file-state foundation; the complete product scope described in the project specification is not yet implemented.

## Current implementation

The repository currently provides:

- a native Android/Jetpack Compose application foundation;
- a real app-private local-storage browser used as the first storage provider;
- Home and Browse surfaces with compact/adaptive navigation behavior;
- a unified file-status domain model that keeps synchronization, backup/recoverability, privacy, security, and continuity state separate;
- explicit `unknown`/`unavailable` evidence states so missing integration is not presented as protection;
- adapter boundaries for GoreeCloud Drive, GoreeCloud Sync, GoreeCloud Backup, Everkeep, Privacy Shield, Wardveil Security, GoreeCloud Identity, and GoreeCloud Mesh;
- repository validation, Android unit tests, lint, and development APK assembly in CI.

App-private browsing is intentionally narrow at this milestone. Broad device storage, Storage Access Framework roots, removable media, network locations, GoreeCloud Drive, cross-device state, destructive file operations, search/indexing, previews, sharing, recovery, and platform service integrations remain implementation work.

## Product direction

The target product is a unified file control center spanning traditional file management, search, previews, tags and collections, operations, storage intelligence, offline availability, sharing, synchronization, backup/recovery, Everkeep continuity, Privacy Shield privacy state, Wardveil security state, GoreeCloud Identity ownership/access state, and GoreeCloud Mesh coordination.

Two non-negotiable product rules are already encoded in the architecture:

- **Sync is not backup.** A synchronized copy or synchronized deletion must never be represented as independent recovery protection.
- **Missing evidence is not reassurance.** Unknown, stale, unavailable, or unverified platform evidence must remain visible as such.

## Native application model

This is an original GoreeCloud-owned application. Complete third-party file-manager forks are not the product architecture. Mature operating-system APIs, Android/Jetpack components, standard protocols, codecs, libraries, and comparable narrowly justified foundations may be used where replacing them would reduce safety or compatibility.

## Mandatory GoreeCloud platform gates

- **Glaze UI:** current target is **Glaze UI 2.0.0 Stable**. This repository has an application-side mapping foundation, but rendered/native accessibility and representative-device acceptance are not complete, so current-Stable conformance is not claimed.
- **Wardveil Security:** integration is required for authoritative security state and applicable file/content protection. No broad “Protected by Wardveil” claim is made by this repository.
- **Privacy Shield:** integration is required for privacy authorization, minimization, exposure state, and privacy-aware file workflows. Runtime acceptance is not yet established here.
- **Everkeep:** integration is required for evidence-backed backup, recoverability, continuity, preservation, and portability state. Backup existence must not be equated with verified recoverability.

GoreeCloud Identity and GoreeCloud Mesh are also first-class platform authorities for identity/access and coordination respectively; their adapter boundaries are established, but runtime integration remains pending.

## Repository documents

- [SPECIFICATIONS.md](SPECIFICATIONS.md) — application specification and current technical baseline
- [FEATURES.md](FEATURES.md) — implemented and planned feature status
- [BENEFITS.md](BENEFITS.md) — intended user and platform benefits
- [COMPETITIVE-OBJECTIVES.md](COMPETITIVE-OBJECTIVES.md) — product-quality objectives, not parity claims
- [ARCHITECTURE.md](ARCHITECTURE.md) — native architecture, provider model, and authority boundaries
- [CONFORMANCE.md](CONFORMANCE.md) — current GoreeCloud platform-gate status

The canonical project record and historical change log are maintained in Google Drive under `GoreeCloud/Projects` and `GoreeCloud/Changelogs`.

## Android development baseline

- Namespace / production application ID: `com.goreecloud.filemanager`
- Development application ID: `com.goreecloud.filemanager.dev`
- Minimum Android API: 26
- Compile API: 37
- Target API: 36
- Java runtime target: 17
- UI: Jetpack Compose / Material 3 with GoreeCloud Glaze UI application mapping

Run validation with:

```bash
python3 scripts/validate_repository.py
gradle :app:testDebugUnitTest
gradle :app:lintDebug
gradle :app:assembleDebug
```

## License

This repository is licensed under the GNU Affero General Public License v3.0. See [LICENSE](LICENSE).
