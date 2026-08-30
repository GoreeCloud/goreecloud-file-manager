# GoreeCloud File Manager

GoreeCloud File Manager is the original GoreeCloud-owned file-management application for browsing and controlling files across supported local, cloud, synchronized, removable, network, backup, and continuity contexts.

> **Development status:** native Android application in active development. This repository is **not Stable or production accepted**. The current implementation now includes user-authorized Android document trees and a first capability-driven mutation slice, but the complete product scope described in the project specification is not yet implemented.

## Current implementation

The repository currently provides:

- a native Android/Jetpack Compose application foundation;
- a real app-private local-storage provider;
- Android Storage Access Framework document-tree authorization using the system picker and persisted URI permissions;
- browsing of app-private storage and user-selected Android document trees without requesting unrestricted filesystem access;
- provider-scoped resource identity and explicit per-item capabilities;
- verified create-folder, rename, and delete operations for supported providers;
- deliberate refusal of recursive folder deletion in this development slice;
- operation-result messaging and refresh-after-operation reconciliation;
- Home and Browse surfaces with compact/adaptive navigation behavior;
- a unified file-status domain model that keeps synchronization, backup/recoverability, privacy, security, and continuity state separate;
- explicit `unknown`/`unavailable` evidence states so missing integration is not presented as protection;
- adapter boundaries for GoreeCloud Drive, GoreeCloud Sync, GoreeCloud Backup, Everkeep, Privacy Shield, Wardveil Security, GoreeCloud Identity, and GoreeCloud Mesh;
- repository validation, Android unit tests, lint, and development APK assembly in CI;
- a repository `USER-MANUAL.md` synchronized with the central GoreeCloud User Manual requirement.

The current storage slice remains intentionally bounded. Copy, move, duplicate, file creation, multi-selection, unified Trash/recovery, removable-storage-specific controls, network locations, GoreeCloud Drive, cross-device state, search/indexing, previews, sharing, and accepted platform-service runtime integrations remain implementation work.

## Authorized Android storage model

File Manager does not equate filesystem access with unrestricted device access.

The built-in app-private provider is confined to the application's own private files root. Broader Android storage is added only through `ACTION_OPEN_DOCUMENT_TREE` / the Android system tree picker. File Manager persists the user-granted tree permission when Android allows it and reconstructs authorized providers from Android's persisted URI-permission state.

A selected tree is treated as an Android document provider, not blindly labeled local disk storage. It may represent local storage, removable media, or another DocumentsProvider. Every visible item retains provider-scoped identity and capability state.

Mutation actions are capability-driven. File Manager exposes create-folder, rename, or delete only when both authorization and the backing provider report support. Recursive folder deletion is intentionally rejected until unified Trash, backup/recovery, Everkeep, and destructive-operation safeguards are implemented and accepted.

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
- [USER-MANUAL.md](USER-MANUAL.md) — current user-facing Android behavior and limitations

The canonical project record and historical change log are maintained in Google Drive under `GoreeCloud/Projects` and `GoreeCloud/Changelogs`. The central user manual is maintained under `GoreeCloud/User Manuals`.

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
