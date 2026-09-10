# GoreeCloud File Manager

GoreeCloud File Manager is the original GoreeCloud-owned file-management application for browsing and controlling files across supported local, cloud, synchronized, removable, network, backup, and continuity contexts.

> **Development status:** native Android application in active development, with a shared JVM File Manager core plus bounded Linux provider/discovery foundations and a development-only Compose Desktop browsing surface now present on the current candidate branch. This repository is **not Stable or production accepted**. Linux and Android are required first-class native product targets. Android remains the only production-shaped native user-facing development client; the Linux desktop surface is an engineering candidate, not a supported Linux package, production runtime, or Stable acceptance.

## Required native platforms

GoreeCloud File Manager must support **Linux and Android** as first-class native platforms. Neither platform is a wrapper around the other.

The product architecture preserves a shared provider/resource identity model, capability semantics, operation safety, transfer verification, GoreeCloud authority boundaries, evidence semantics, and user-visible state while using platform-specific storage, permissions, lifecycle, input, packaging, and system integration.

- **Android:** app-private storage, user-authorized Android document trees, scoped/least-privilege storage behavior, Android Open/Save/share integration, touch-first adaptive layouts, and Android lifecycle/permission semantics.
- **Linux:** native desktop filesystem/provider integration, authorized paths and XDG locations, mounts/removable storage, Unix permissions/ownership, symbolic links, file associations/Open With, drag-and-drop, keyboard/pointer workflows, windows/tabs/dual-pane patterns, and supported network/provider integration.

A Linux filesystem path, Android document/content URI, GoreeCloud Drive resource ID, and remote-provider identifier remain distinct provider-scoped identities. Cross-platform behavior must not pretend one platform's native identifier is universally valid on another.

## Current implementation

The repository currently provides:

- a native Android/Jetpack Compose application foundation;
- a shared JVM `:core` module consumed by Android and the Linux development module, owning provider-scoped file/status models, provider contracts, file-name policy, GoreeCloud authority boundaries, and SHA-256-verified ordinary-file transfer semantics;
- a real Android app-private local-storage provider;
- Android Storage Access Framework document-tree authorization using the system picker and persisted URI permissions;
- browsing of app-private storage and user-selected Android document trees without requesting unrestricted filesystem access;
- provider-scoped resource identity and explicit per-item capabilities;
- verified create-folder, rename, and delete operations for supported Android providers;
- a provider-generic regular-file copy/move transfer service that streams source bytes, publishes through the destination provider, reopens the destination, and requires matching SHA-256 content before a move may remove its source;
- deliberate refusal of recursive folder deletion and recursive folder transfer in this development slice;
- operation-result messaging and refresh-after-operation reconciliation;
- Android Home and Browse surfaces with compact/adaptive navigation behavior;
- a unified file-status domain model that keeps synchronization, backup/recoverability, privacy, security, and continuity state separate;
- explicit `unknown`/`unavailable` evidence states so missing integration is not presented as protection;
- adapter boundaries for GoreeCloud Drive, GoreeCloud Sync, GoreeCloud Backup, Everkeep, Privacy Shield, Wardveil Security, GoreeCloud Identity, and GoreeCloud Mesh;
- a `:linux-client` JVM development module that consumes the shared core and establishes the Linux provider/discovery/application boundary without claiming production Linux acceptance;
- a bounded Linux local-filesystem provider rooted at one explicit directory, with provider-relative resource identity, path-escape rejection, symbolic-link visibility without traversal/mutation, and mutation capabilities withheld across detected mount/FileStore boundaries;
- Linux create-file, create-folder, rename, ordinary-file read/write, shared verified copy/move participation, and empty-only folder deletion primitives within that bounded provider;
- read-only Linux location-candidate discovery for Home, supported XDG user directories, mounted filesystems, and removable-media candidates; every discovered location still requires explicit selection before a provider exists;
- a non-production Linux command-line development harness that performs either an explicit-root read-only listing or a read-only `--locations` candidate report;
- a development-only Compose Multiplatform Desktop `1.12.0` presentation surface with an edge location sidebar, responsive toolbar, solid file-content plane, contextual inspector, and read-only provider-scoped folder browsing;
- a Linux desktop controller that structurally separates discovery/highlighting from provider construction: only the explicit **Open location** action may create the bounded provider, and returning to Locations closes that provider boundary;
- unit tests proving the desktop discovery/highlight path is non-authorizing, explicit open constructs the provider, navigation stays provider-scoped, failed open leaves no provider authorized, and returning to Locations clears the active provider;
- repository validation plus independent Android and Linux development workflow definitions; exact-head workflow results remain the acceptance evidence for each candidate revision;
- a repository `FEATURE-ROADMAP.md` synchronized with the central GoreeCloud File Manager feature-roadmap control;
- a repository `USER-MANUAL.md` synchronized with the central GoreeCloud User Manual requirement.

The current storage slice remains intentionally bounded. The Android backend has regular-file copy/move primitives, but the Android UI does not yet expose destination-selection copy/move workflows. The Linux desktop candidate exposes location selection and read-only folder browsing only; existing Linux mutation primitives are deliberately not surfaced there. Duplicate, user-facing file creation on Android, multi-selection, unified Trash/recovery, recursive folder transfer, removable-storage lifecycle/safe-eject controls, network-provider workflows, GoreeCloud Drive, cross-device state, search/indexing, previews, sharing, and accepted platform-service runtime integrations remain implementation work.

## Linux development boundary

Linux implementation source now exists, but the distinction between **development foundation** and **supported Linux client** remains explicit.

The current Linux provider:

- requires one explicitly selected root directory;
- keeps resource IDs provider-relative and rejects normalized paths that escape that root;
- reads filesystem metadata without following symbolic links;
- represents symbolic links as `SYMLINK` resources with no traversal or mutation capabilities in the current slice;
- withholds mutations across a detected mount/FileStore boundary until mount-specific policy is implemented and accepted;
- permits bounded ordinary-file and empty-folder mutations only where operating-system permissions and provider capabilities allow them;
- reuses the shared SHA-256-verified transfer service;
- refuses recursive folder deletion and recursive folder transfer.

The current Linux location-discovery layer is **read-only metadata discovery, not authorization**. It can report Home, recognized XDG user directories, user-facing mount points from `/proc/self/mountinfo`, and `/media` or `/run/media` paths as removable-media candidates. XDG parsing expands only literal `$HOME` / `${HOME}` forms or accepts explicit absolute paths; it does not execute shell expressions. Candidate directories must exist and must not themselves be symbolic links. A mount path being classified as a removable-media candidate does not prove that the device is removable, safe to eject, or authorized for mutation.

Every discovery result carries an explicit-selection requirement. Discovery does not construct `LinuxFileRepository`, grant file access, or widen the provider root. The provider is created only after explicit selection/open and remains the Linux file-access boundary.

### Linux desktop development surface

The current candidate adds a non-production Compose Desktop surface rather than relabeling the CLI harness as a desktop application. It presents discovered location candidates first. Highlighting a candidate remains metadata-only; the separate **Open location** action is required before `LinuxFileRepository` is constructed. The resulting desktop browsing session is read-only at the UI layer even when the provider reports mutation capabilities.

The desktop composition follows the current GLAZE UI V1.3 direction at source/design level: content-first window composition, edge-integrated location navigation, responsive toolbar, solid file-content plane, contextual information, clear state/error messaging, and deliberate avoidance of a stretched mobile layout. This source implementation is **not** a Glaze conformance certificate. Rendered/native accessibility, keyboard/pointer, reduced-motion/transparency/contrast behavior, representative desktop review, and current consumer acceptance remain pending.

Run the development surface from a Linux graphical session with:

```bash
gradle :linux-client:runDesktopDevelopment
```

This command is a developer workflow, not a supported Linux installation or release procedure.

### Command-line development harness

The current Linux harness remains intentionally read-only at its user-facing command surface. Explicit-root mode lists one selected provider root. `--locations` prints read-only discovery candidates without granting provider authorization. It remains useful for headless provider/discovery/build validation independently of the graphical development surface.

A passing Linux development workflow establishes exact-revision source/build/test evidence for this bounded development slice only. It does **not** by itself make Linux a supported platform in `goreecloud.platform.yaml`, establish production packaging, prove desktop accessibility/input integration, or permit a Stable claim.

## Authorized Android storage model

File Manager does not equate filesystem access with unrestricted device access.

The built-in app-private provider is confined to the application's own private files root. Broader Android storage is added only through `ACTION_OPEN_DOCUMENT_TREE` / the Android system tree picker. File Manager persists the user-granted tree permission when Android allows it and reconstructs authorized providers from Android's persisted URI-permission state.

A selected tree is treated as an Android document provider, not blindly labeled local disk storage. It may represent local storage, removable media, or another DocumentsProvider. Every visible item retains provider-scoped identity and capability state.

Mutation actions are capability-driven. File Manager exposes create-folder, rename, or delete only when both authorization and the backing provider report support. Recursive folder deletion is intentionally rejected until unified Trash, backup/recovery, Everkeep, and destructive-operation safeguards are implemented and accepted. Copy/move remains service-level foundation only until destination selection, user-facing conflict handling, operation UX, and the applicable destructive/recovery safeguards are connected to the UI.

## Product direction

The target product is a unified file control center spanning traditional file management, search, previews, tags and collections, operations, storage intelligence, offline availability, sharing, synchronization, backup/recovery, Everkeep continuity, Privacy Shield privacy state, Wardveil security state, GoreeCloud Identity ownership/access state, and GoreeCloud Mesh coordination across Linux and Android.

Two non-negotiable product rules are already encoded in the architecture:

- **Sync is not backup.** A synchronized copy or synchronized deletion must never be represented as independent recovery protection.
- **Missing evidence is not reassurance.** Unknown, stale, unavailable, or unverified platform evidence must remain visible as such.

## Native application model

This is an original GoreeCloud-owned application. Complete third-party file-manager forks are not the product architecture. Mature operating-system APIs, Android/Jetpack components, Linux standards and libraries, standard protocols, codecs, and comparable narrowly justified foundations may be used where replacing them would reduce safety, compatibility, accessibility, interoperability, or maintainability.

The cross-platform architecture separates platform-neutral File Manager domain/provider/operation contracts from Android- and Linux-specific adapters. Platform-native behavior is required; Android storage semantics must not be imposed on Linux, and Linux path semantics must not be imposed on Android document providers.

Compose Multiplatform Desktop `1.12.0` is the current Linux **development presentation toolkit** because it fits the existing Kotlin/JVM module and supports purpose-built desktop composition. This does not make the Android and Linux layouts shared or interchangeable, and it does not establish supported Linux packaging, representative runtime acceptance, or a permanent production release decision by itself.

## Mandatory GoreeCloud platform gates

- **Glaze UI:** current governed target is **GLAZE UI V1.3 / 1.3.0 Stable**. The Linux candidate now has a real desktop presentation source surface, but it remains development-only and has not completed rendered/native accessibility, adaptive/input, reduced-transparency/contrast/motion, or representative-platform acceptance. Android also still requires fresh current-revision migration/acceptance. Current-Stable conformance is not claimed.
- **Wardveil Security:** integration is required for authoritative security state and applicable file/content protection. No broad “Protected by Wardveil” claim is made by this repository.
- **Privacy Shield:** integration is required for privacy authorization, minimization, exposure state, and privacy-aware file workflows. Runtime acceptance is not yet established here.
- **Everkeep:** integration is required for evidence-backed backup, recoverability, continuity, preservation, and portability state. Backup existence must not be equated with verified recoverability.

GoreeCloud Identity and GoreeCloud Mesh are also first-class platform authorities for identity/access and coordination respectively; their adapter boundaries are established, but runtime integration remains pending.

## Repository documents

- [SPECIFICATIONS.md](SPECIFICATIONS.md) — application specification and current technical baseline
- [FEATURES.md](FEATURES.md) — implemented and planned feature status
- [FEATURE-ROADMAP.md](FEATURE-ROADMAP.md) — active feature obligations, priority, dependency/acceptance state, and Drive-roadmap synchronization control
- [BENEFITS.md](BENEFITS.md) — intended user and platform benefits
- [COMPETITIVE-OBJECTIVES.md](COMPETITIVE-OBJECTIVES.md) — product-quality objectives, not parity claims
- [ARCHITECTURE.md](ARCHITECTURE.md) — native architecture, provider model, platform split, and authority boundaries
- [CONFORMANCE.md](CONFORMANCE.md) — current GoreeCloud platform-gate status
- [USER-MANUAL.md](USER-MANUAL.md) — current Android behavior plus bounded Linux CLI/desktop development availability and acceptance boundaries

The canonical project record and historical change log are maintained in Google Drive under `GoreeCloud/Projects` and `GoreeCloud/Changelogs`. The central feature roadmap is maintained under `GoreeCloud/Feature Roadmap/GoreeCloud File Manager`, and the central user manual is maintained under `GoreeCloud/User Manuals`.

## Android development baseline

- Namespace / production application ID: `com.goreecloud.filemanager`
- Development application ID: `com.goreecloud.filemanager.dev`
- Minimum Android API: 26
- Compile API: 37
- Target API: 36
- Java runtime target: 17
- UI: Jetpack Compose / Material 3 with GoreeCloud Glaze UI application mapping

Run current Android validation with:

```bash
python3 scripts/validate_repository.py
gradle :core:test :app:testDebugUnitTest
gradle :app:lintDebug
gradle :app:assembleDebug
```

## Linux development baseline

The current Linux source baseline is Kotlin/JVM 17 with Compose Multiplatform Desktop `1.12.0` for the graphical development surface. The module consumes the same `:core` contracts as Android while retaining Linux-specific provider, discovery, authorization, and desktop presentation behavior.

Run the bounded Linux development checks with:

```bash
python3 scripts/validate_repository.py
gradle :core:test :linux-client:test
gradle :linux-client:classes
gradle :linux-client:installDist :linux-client:distTar
./linux-client/build/install/linux-client/bin/linux-client /an/explicit/root
./linux-client/build/install/linux-client/bin/linux-client --locations
```

Run the non-production desktop surface from a Linux graphical session with:

```bash
gradle :linux-client:runDesktopDevelopment
```

The explicit-root command performs a read-only provider listing. `--locations` performs read-only location-candidate discovery and does not grant provider authorization. The graphical development surface also keeps discovery and provider construction separate and exposes read-only browsing only.

Exact Linux package formats are intentionally not claimed yet. The generated Gradle distribution is development evidence, not a supported Debian, Flatpak, AppImage, RPM, Snap, or other production package. Packaging and distribution choices must be validated against GoreeCloud release requirements before any format is called supported.

## License

This repository is licensed under the GNU Affero General Public License v3.0. See [LICENSE](LICENSE).
