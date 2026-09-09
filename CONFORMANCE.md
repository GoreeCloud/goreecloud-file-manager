# GoreeCloud File Manager — Conformance and Acceptance

## Current lifecycle

**Active Android development with shared-core and bounded Linux-provider/location-discovery development. Production and Stable eligibility: false.**

Linux and Android are required first-class native product platforms. Android is the current native user-facing implementation. The repository now contains a shared JVM `:core` module plus a bounded Linux local-filesystem provider, read-only Home/XDG/mount location-candidate discovery, provider/discovery tests, and a non-production command-line development harness. This does **not** establish an accepted Linux desktop client, supported Linux package, production runtime, or Stable Linux support.

The current development milestone therefore distinguishes three levels of truth:

1. shared/core and Linux development source is present;
2. exact-head workflow completion is required before that candidate's source/build checks count as evidence;
3. even successful development CI does not establish native desktop/package/runtime/Stable acceptance.

| Gate | Required baseline | Current File Manager state | Production gate |
| --- | --- | --- | --- |
| Native application model | Original GoreeCloud-owned product | Native Android application plus shared-core/platform-adapter architecture established | In progress |
| Required platform scope | First-class Linux + Android | Android native client exists; Linux development provider/discovery/build boundary exists; accepted desktop client/package/runtime absent | Blocked |
| Shared cross-platform core | Platform-neutral provider/resource/operation/evidence contracts | `:core` consumed by Android and Linux development module; exact candidate validation required | In progress |
| Android storage authorization | Least-privilege, provider-bounded access | App-private confinement plus user-selected persisted document trees implemented | In progress |
| Linux filesystem/provider integration | Native bounded Linux filesystem/provider behavior | Explicit-root provider, symlink/path/mount safeguards, read-only Home/XDG/mount candidate discovery, tests, harness, and Linux workflow definition present; broader desktop/provider acceptance incomplete | In progress / blocked for production |
| Core file operations | Safe, capability-aware, reconcilable operations | Bounded mutation + SHA-256-verified ordinary-file transfer foundation; complete UI/Trash/recursive workflows incomplete | Blocked |
| Glaze UI | GLAZE UI V1.3 / 1.3.0 Stable | Central Platform Contract baseline is V1.3; Android mapping still requires current consumer migration/acceptance and Linux harness has no desktop Glaze UI | Blocked |
| Wardveil Security | Current approved Wardveil contracts | Adapter boundary only; no accepted runtime evidence | Blocked |
| Privacy Shield | Current approved Privacy Shield contract/runtime authority | Adapter boundary only; no accepted runtime evidence | Blocked |
| Everkeep | Current approved continuity/recovery contract | Adapter boundary only; no accepted runtime evidence | Blocked |
| GoreeCloud Identity | Current approved identity/access authority | Adapter boundary only | In progress |
| GoreeCloud Mesh | Current approved coordination/evidence profile | Adapter boundary only | In progress |

`goreecloud.platform.yaml` intentionally remains Android-only in `supported_platforms` until Linux satisfies the machine-readable supported-platform evidence requirements. A Linux development module or discovery layer existing is not sufficient reason to broaden that field.

## Android storage acceptance boundary

The current Storage Access Framework integration is a least-privilege source implementation:

- broader storage is selected through Android's system document-tree picker;
- File Manager requests persistable read permission and write permission when granted;
- persisted permissions are used to reconstruct authorized locations;
- resource identity remains bounded to the selected tree authority and tree document ID;
- mutation UI is driven by persisted authorization plus provider-reported document capabilities;
- the selected document tree is not falsely classified as local disk when its provider type is unknown.

This source behavior still requires representative-device/provider validation. It does not establish support for every Android DocumentsProvider.

## Linux development acceptance boundary

The current `LinuxFileRepository` is a deliberately bounded source implementation, not the final Linux filesystem contract.

Current source safeguards include:

- one explicit existing root directory per provider instance;
- provider-relative IDs and normalized root confinement;
- selected root may not itself be a symbolic link;
- metadata inspection with `NOFOLLOW_LINKS`;
- symbolic links represented as non-traversable/non-mutable `SYMLINK` resources in the current slice;
- symbolic-link component traversal rejected;
- operating-system readability/writability respected;
- mutation capability withheld when a resource's `FileStore` differs from the selected root's store;
- provider root rename/delete rejected;
- recursive directory deletion refused;
- shared verified ordinary-file copy/move supported by provider primitives and exercised in Linux test source;
- command-line development harness accepts one explicit root and exposes read-only listing only.

The current `LinuxLocationDiscovery` layer is separately bounded and **does not grant provider access**. It may discover:

- the current Home directory;
- recognized XDG user directories from `user-dirs.dirs`;
- user-facing mounted filesystems parsed from `/proc/self/mountinfo`;
- `/media` and `/run/media` paths as removable-media **candidates**.

All discovery entries require explicit selection before File Manager constructs a provider. XDG parsing expands only literal `$HOME` / `${HOME}` forms or accepts explicit absolute paths; it does not execute shell expressions. Discovery includes only existing non-symlink directories under its current checks, filters ordinary pseudo/system-only mount surfaces, and does not treat a removable-media candidate as proof of hardware removability, safe-eject support, or mutation authority.

`LinuxDevelopmentMain --locations` exposes only this read-only candidate report. The existing explicit-root mode remains the provider-access development boundary. No discovered location is automatically opened, authorized, or made mutable.

Exact-head Linux development CI is required to establish candidate-specific source/build/test evidence for these behaviors. That workflow checks repository contracts, shared-core/Linux tests, JVM development-distribution construction, explicit-root smoke behavior, a distribution digest, and an explicit development-only artifact boundary.

Even after those checks pass, Linux production acceptance still requires the native desktop application surface, user-facing XDG location/navigation policy, mount/removable-media lifecycle and safe-eject integration, reviewed permissions/ownership/symlink policy beyond the bounded slice, desktop keyboard/pointer/accessibility integration, applicable file-association/Open With and drag-and-drop behavior, supported distribution/desktop-environment scope, accepted package/signing/release provenance, representative runtime testing, current Glaze acceptance, and applicable Platform-System evidence.

Android source/build evidence cannot be reused as Linux acceptance.

## Mutation acceptance boundary

Current provider operations distinguish successful, rejected, and failed results. App-private path confinement, shared transfer integrity/failure behavior, and Linux bounded provider safety behavior have corresponding test source.

Recursive folder deletion and recursive folder transfer are deliberately refused. Complete destination-selection copy/move UI, complete duplicate/create-file UI, multi-selection, conflict handling, durable Operations Center, unified Trash, rollback/recovery, and platform-authority preflight remain incomplete. Therefore broad destructive-operation acceptance is blocked.

A provider reporting a filesystem capability does not establish Privacy Shield authorization, Wardveil approval, backup protection, or Everkeep recoverability.

## Glaze UI acceptance requirements

File Manager may claim current-Stable Glaze UI alignment only after its exact revision demonstrates repository-local **GLAZE UI V1.3 / 1.3.0** adoption, automated validation, rendered/native accessibility, responsive/form-factor behavior, state presentation, and representative platform acceptance appropriate to each supported Linux and Android context.

The current Android source's historical Glaze mapping foundation is not a conformance certificate and cannot be relabeled as V1.3 acceptance without fresh migration evidence. The Linux command-line development harness is not a desktop Glaze UI surface and supplies no visual/accessibility conformance evidence.

The authoritative central Platform Contract has been reconciled to GLAZE UI V1.3 / `1.3.0` at merged immutable revision `235e519fe342d7e7075c8239fbf0f3a19dc4c6c8`. File Manager's reusable Platform Contract workflow is pinned to that exact revision. This resolves the stale shared-validator baseline mismatch; it does **not** establish File Manager's rendered/native Glaze UI acceptance, which remains independently blocked on application-specific migration and evidence.

## Wardveil claim rules

A file may be represented with a positive Wardveil security outcome only when the application has current authoritative Wardveil evidence for the exact resource/content scope and applicable evidence is valid. Unsupported, stale, expired, unavailable, unknown, or unverified coverage cannot become a clean/protected claim.

## Privacy Shield claim rules

Operating-system resource authorization, Android document-tree authorization, Linux filesystem access, or file ownership does not itself establish privacy authorization for indexing, remote processing, telemetry, sharing, synchronization, backup, or other distinct purposes. Privacy-relevant operations must use current applicable Privacy Shield authority. Missing runtime acceptance blocks a positive Privacy Shield coverage claim.

## Everkeep claim rules

`backup exists`, `integrity verified`, `restore tested`, and `recoverable` are distinct states. File Manager must not show verified recoverability unless the applicable Everkeep/backup evidence establishes it for the resource and recovery scope.

## Source/build acceptance

An exact development revision is expected to pass all applicable checks for the platform code present in that revision.

Current Android workflow checks include:

- exact source-revision checkout and recording;
- repository/document validation, including the required `USER-MANUAL.md`;
- shared-core unit tests;
- Android unit tests;
- Android lint;
- development APK assembly;
- package/application-label verification;
- exact-revision artifact publication.

Current Linux development workflow checks include:

- exact source-revision checkout and recording;
- repository/document validation;
- shared-core unit tests;
- Linux provider and location-discovery unit tests;
- JVM development-distribution construction;
- explicit-root development-harness smoke test;
- distribution SHA-256 digest;
- an evidence file stating that the artifact is not an accepted Linux desktop package or Stable release.

The shared Platform Contract workflow remains an independent required gate and is pinned to the accepted central V1.3 validator revision. A successful manifest/conformance run proves only the Platform Contract checks performed for that exact caller revision; it does not upgrade application-specific Glaze UI, security, privacy, recovery, Linux desktop, or Stable acceptance.

Passing source/build checks establishes only the checks actually performed for that exact revision. It does not establish platform-runtime production acceptance, representative-platform compatibility, production signing/deployment, Linux desktop UI/package support, complete mount/removable-media lifecycle behavior, complete Platform-System integration, or Stable qualification.
