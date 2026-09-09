# GoreeCloud File Manager — Conformance and Acceptance

## Current lifecycle

**Active Android development with shared-core and bounded Linux-provider development. Production and Stable eligibility: false.**

Linux and Android are required first-class native product platforms. Android is the current native user-facing implementation. The repository now contains a shared JVM `:core` module plus a bounded Linux local-filesystem provider, provider tests, and non-production command-line development harness. This does **not** establish an accepted Linux desktop client, supported Linux package, production runtime, or Stable Linux support.

The current development milestone therefore distinguishes three levels of truth:

1. shared/core and Linux development source is present;
2. exact-head workflow completion is required before that candidate's source/build checks count as evidence;
3. even successful development CI does not establish native desktop/package/runtime/Stable acceptance.

| Gate | Required baseline | Current File Manager state | Production gate |
| --- | --- | --- | --- |
| Native application model | Original GoreeCloud-owned product | Native Android application plus shared-core/platform-adapter architecture established | In progress |
| Required platform scope | First-class Linux + Android | Android native client exists; Linux development provider/build boundary exists; accepted desktop client/package/runtime absent | Blocked |
| Shared cross-platform core | Platform-neutral provider/resource/operation/evidence contracts | `:core` consumed by Android and Linux development module; exact candidate validation required | In progress |
| Android storage authorization | Least-privilege, provider-bounded access | App-private confinement plus user-selected persisted document trees implemented | In progress |
| Linux filesystem/provider integration | Native bounded Linux filesystem/provider behavior | Explicit-root provider source, symlink/path/mount safeguards, tests, harness, and Linux workflow definition present; broader desktop/provider acceptance incomplete | In progress / blocked for production |
| Core file operations | Safe, capability-aware, reconcilable operations | Bounded mutation + SHA-256-verified ordinary-file transfer foundation; complete UI/Trash/recursive workflows incomplete | Blocked |
| Glaze UI | GLAZE UI V1.3 / 1.3.0 Stable | Android mapping predates current target; Linux harness has no desktop Glaze UI; fresh Linux/Android conformance evidence absent | Blocked |
| Wardveil Security | Current approved Wardveil contracts | Adapter boundary only; no accepted runtime evidence | Blocked |
| Privacy Shield | Current approved Privacy Shield contract/runtime authority | Adapter boundary only; no accepted runtime evidence | Blocked |
| Everkeep | Current approved continuity/recovery contract | Adapter boundary only; no accepted runtime evidence | Blocked |
| GoreeCloud Identity | Current approved identity/access authority | Adapter boundary only | In progress |
| GoreeCloud Mesh | Current approved coordination/evidence profile | Adapter boundary only | In progress |

`goreecloud.platform.yaml` intentionally remains Android-only in `supported_platforms` until Linux satisfies the machine-readable supported-platform evidence requirements. A Linux development module existing is not sufficient reason to broaden that field.

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

Exact-head Linux development CI is required to establish candidate-specific source/build/test evidence for these behaviors. That workflow checks repository contracts, shared-core/Linux tests, JVM development-distribution construction, explicit-root smoke behavior, a distribution digest, and an explicit development-only artifact boundary.

Even after those checks pass, Linux production acceptance still requires the native desktop application surface, XDG/mount/removable-media integration, reviewed permissions/ownership/symlink policy beyond the bounded slice, desktop keyboard/pointer/accessibility integration, applicable file-association/Open With and drag-and-drop behavior, supported distribution/desktop-environment scope, accepted package/signing/release provenance, representative runtime testing, current Glaze acceptance, and applicable Platform-System evidence.

Android source/build evidence cannot be reused as Linux acceptance.

## Mutation acceptance boundary

Current provider operations distinguish successful, rejected, and failed results. App-private path confinement, shared transfer integrity/failure behavior, and Linux bounded provider safety behavior have corresponding test source.

Recursive folder deletion and recursive folder transfer are deliberately refused. Complete destination-selection copy/move UI, complete duplicate/create-file UI, multi-selection, conflict handling, durable Operations Center, unified Trash, rollback/recovery, and platform-authority preflight remain incomplete. Therefore broad destructive-operation acceptance is blocked.

A provider reporting a filesystem capability does not establish Privacy Shield authorization, Wardveil approval, backup protection, or Everkeep recoverability.

## Glaze UI acceptance requirements

File Manager may claim current-Stable Glaze UI alignment only after its exact revision demonstrates repository-local **GLAZE UI V1.3 / 1.3.0** adoption, automated validation, rendered/native accessibility, responsive/form-factor behavior, state presentation, and representative platform acceptance appropriate to each supported Linux and Android context.

The current Android source's historical Glaze mapping foundation is not a conformance certificate and cannot be relabeled as V1.3 acceptance without fresh migration evidence. The Linux command-line development harness is not a desktop Glaze UI surface and supplies no visual/accessibility conformance evidence.

The repository's shared Platform Contract validator is currently pinned to an older immutable central implementation whose Glaze baseline is stale relative to the canonical V1.3 requirement. File Manager must not downgrade its truthful `1.3.0` requirement merely to satisfy that stale validator; the central contract discrepancy is tracked separately and remains a validation blocker until the authoritative shared validator is reconciled and the repository pin is intentionally upgraded.

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
- Linux provider unit tests;
- JVM development-distribution construction;
- explicit-root development-harness smoke test;
- distribution SHA-256 digest;
- an evidence file stating that the artifact is not an accepted Linux desktop package or Stable release.

The shared Platform Contract workflow remains an independent required gate. Its current Glaze-baseline failure must be resolved in the authoritative central contract rather than suppressed locally.

Passing source/build checks establishes only the checks actually performed for that exact revision. It does not establish platform-runtime production acceptance, representative-platform compatibility, production signing/deployment, Linux desktop UI/package support, complete Platform-System integration, or Stable qualification.
