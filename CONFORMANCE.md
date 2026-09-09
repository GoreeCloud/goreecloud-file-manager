# GoreeCloud File Manager — Conformance and Acceptance

## Current lifecycle

**Active Android development. Production and Stable eligibility: false.**

Linux and Android are required first-class native product platforms. The current source/build implementation remains Android-only. No Linux source/build/package/runtime acceptance is established by the current repository state.

The native Android foundation is merged. The current development milestone adds user-authorized Android document-tree providers, bounded file mutations, and provider-generic verified regular-file transfer foundations. This record distinguishes implementation progress from accepted conformance.

| Gate | Required baseline | Current File Manager state | Production gate |
| --- | --- | --- | --- |
| Native application model | Original GoreeCloud-owned product | Native application and provider architecture established | In progress |
| Required platform scope | First-class Linux + Android | Android implementation exists; Linux implementation/evidence absent | Blocked |
| Android storage authorization | Least-privilege, provider-bounded access | App-private confinement plus user-selected persisted document trees implemented | In progress |
| Linux filesystem/provider integration | Native bounded Linux filesystem/provider behavior | Required by specification; no Linux client/provider/build evidence yet | Blocked |
| Core file operations | Safe, capability-aware, reconcilable operations | Bounded mutation + verified regular-file transfer foundation; complete UI/Trash/recursive workflows incomplete | Blocked |
| Glaze UI | GLAZE UI V1.3 / 1.3.0 Stable | Existing application mapping predates current target; fresh Linux/Android conformance evidence absent | Blocked |
| Wardveil Security | Current approved Wardveil contracts | Adapter boundary only; no accepted runtime evidence | Blocked |
| Privacy Shield | Current approved Privacy Shield contract/runtime authority | Adapter boundary only; no accepted runtime evidence | Blocked |
| Everkeep | Current approved continuity/recovery contract | Adapter boundary only; no accepted runtime evidence | Blocked |
| GoreeCloud Identity | Current approved identity/access authority | Adapter boundary only | In progress |
| GoreeCloud Mesh | Current approved coordination/evidence profile | Adapter boundary only | In progress |

## Android storage acceptance boundary

The current Storage Access Framework integration is a least-privilege source implementation:

- broader storage is selected through Android's system document-tree picker;
- File Manager requests persistable read permission and write permission when granted;
- persisted permissions are used to reconstruct authorized locations;
- resource identity remains bounded to the selected tree authority and tree document ID;
- mutation UI is driven by persisted authorization plus provider-reported document capabilities;
- the selected document tree is not falsely classified as local disk when its provider type is unknown.

This source behavior still requires representative-device/provider validation. It does not establish support for every Android DocumentsProvider.

## Linux acceptance boundary

Linux is a required platform target but has no accepted implementation in the current repository.

Linux acceptance requires, at minimum, an exact-revision native Linux application/build surface; a bounded Linux filesystem/provider implementation; path, permission, ownership, symlink, mount/removable-media, and operation-failure validation; desktop keyboard/pointer/accessibility integration; applicable file-association/Open With and drag-and-drop behavior; supported distribution/desktop-environment scope; packaging/signing/release provenance; and independent current Glaze/platform-system acceptance evidence.

Android source/build evidence cannot be reused as Linux acceptance.

## Mutation acceptance boundary

Current provider operations distinguish successful, rejected, and failed results. App-private path confinement, basic mutation behavior, and regular-file transfer integrity/failure behavior are unit-tested.

Recursive folder deletion and recursive folder transfer are deliberately refused. Complete destination-selection copy/move UI, complete duplicate/create-file UI, multi-selection, conflict handling, durable Operations Center, unified Trash, rollback/recovery, and platform-authority preflight remain incomplete. Therefore broad destructive-operation acceptance is blocked.

A provider reporting a filesystem capability does not establish Privacy Shield authorization, Wardveil approval, backup protection, or Everkeep recoverability.

## Glaze UI acceptance requirements

File Manager may claim current-Stable Glaze UI alignment only after its exact revision demonstrates repository-local **GLAZE UI V1.3 / 1.3.0** adoption, automated validation, rendered/native accessibility, responsive/form-factor behavior, state presentation, and representative platform acceptance appropriate to each supported Linux and Android context.

The current source's historical Glaze mapping foundation is not a conformance certificate and cannot be relabeled as V1.3 acceptance without fresh migration evidence.

## Wardveil claim rules

A file may be represented with a positive Wardveil security outcome only when the application has current authoritative Wardveil evidence for the exact resource/content scope and applicable evidence is valid. Unsupported, stale, expired, unavailable, unknown, or unverified coverage cannot become a clean/protected claim.

## Privacy Shield claim rules

Operating-system resource authorization, Android document-tree authorization, Linux filesystem access, or file ownership does not itself establish privacy authorization for indexing, remote processing, telemetry, sharing, synchronization, backup, or other distinct purposes. Privacy-relevant operations must use current applicable Privacy Shield authority. Missing runtime acceptance blocks a positive Privacy Shield coverage claim.

## Everkeep claim rules

`backup exists`, `integrity verified`, `restore tested`, and `recoverable` are distinct states. File Manager must not show verified recoverability unless the applicable Everkeep/backup evidence establishes it for the resource and recovery scope.

## Source/build acceptance

An exact development revision is expected to pass all applicable checks for the platform code present in that revision.

Current Android checks include:

- repository/document validation, including the required `USER-MANUAL.md`;
- Android unit tests;
- Android lint;
- development APK assembly;
- package/application-label verification;
- exact-revision evidence capture and artifact publication.

Linux must add its own repository-local build, test, packaging/identity, and platform validation checks before Linux source/build acceptance can be claimed or `supported_platforms` can truthfully include Linux.

Passing source/build checks establishes only the checks actually performed for that exact revision. It does not establish platform-runtime production acceptance, representative-platform compatibility, production signing/deployment, or Stable qualification.
