# GoreeCloud File Manager — Conformance and Acceptance

## Current lifecycle

**Active Android development. Production and Stable eligibility: false.**

The native foundation is merged. The current development milestone adds user-authorized Android document-tree providers and initial capability-driven create-folder, rename, and non-recursive delete behavior. This record distinguishes implementation progress from accepted conformance.

| Gate | Required baseline | Current File Manager state | Production gate |
| --- | --- | --- | --- |
| Native application model | Original GoreeCloud-owned product | Native application and provider architecture established | In progress |
| Android storage authorization | Least-privilege, provider-bounded access | App-private confinement plus user-selected persisted document trees implemented | In progress |
| Core file operations | Safe, capability-aware, reconcilable operations | Create-folder/rename/non-recursive delete development slice; copy/move/Trash incomplete | Blocked |
| Glaze UI | 2.0.0 Stable | Target/mapping foundation; rendered and device acceptance pending | Blocked |
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

This source behavior still requires exact-revision CI and representative-device/provider validation. It does not establish support for every Android DocumentsProvider.

## Mutation acceptance boundary

Current create-folder, rename, and delete implementations distinguish successful, rejected, and failed results. App-private path confinement and basic mutation behavior are unit-tested.

Recursive folder deletion is deliberately refused. Copy, move, duplicate, create-file, multi-selection, conflict handling, durable Operations Center, unified Trash, rollback/recovery, and platform-authority preflight remain incomplete. Therefore broad destructive-operation acceptance is blocked.

A provider reporting a filesystem capability does not establish Privacy Shield authorization, Wardveil approval, backup protection, or Everkeep recoverability.

## Glaze UI acceptance requirements

File Manager may claim current-Stable Glaze UI alignment only after its exact revision demonstrates repository-local 2.0.0 mapping, automated validation, rendered/native accessibility, responsive/form-factor behavior, state presentation, and representative real-device acceptance appropriate to supported Android contexts.

The current source target is not a conformance certificate.

## Wardveil claim rules

A file may be represented with a positive Wardveil security outcome only when the application has current authoritative Wardveil evidence for the exact resource/content scope and applicable evidence is valid. Unsupported, stale, expired, unavailable, unknown, or unverified coverage cannot become a clean/protected claim.

## Privacy Shield claim rules

Android resource authorization or file ownership does not itself establish privacy authorization for indexing, remote processing, telemetry, sharing, synchronization, backup, or other distinct purposes. Privacy-relevant operations must use current applicable Privacy Shield authority. Missing runtime acceptance blocks a positive Privacy Shield coverage claim.

## Everkeep claim rules

`backup exists`, `integrity verified`, `restore tested`, and `recoverable` are distinct states. File Manager must not show verified recoverability unless the applicable Everkeep/backup evidence establishes it for the resource and recovery scope.

## Source/build acceptance

An exact development revision is expected to pass:

- repository/document validation, including the required `USER-MANUAL.md`;
- Android unit tests;
- Android lint;
- development APK assembly;
- package/application-label verification;
- exact-revision evidence capture and artifact publication.

Passing these checks establishes only source/build acceptance for that exact revision. It does not establish platform-runtime production acceptance, representative-device compatibility, production signing/deployment, or Stable qualification.
