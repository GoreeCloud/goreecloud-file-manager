# GoreeCloud File Manager — Architecture

## 1. Native product boundary

GoreeCloud File Manager is an original GoreeCloud application. Android/Jetpack APIs and other mature foundational components may be used, but product architecture, file-state semantics, application workflows, GoreeCloud integrations, and user experience remain GoreeCloud-owned.

## 2. Layer model

```text
Glaze UI application surfaces
        ↓
File Manager application/use cases
        ↓
Unified file model + operation/evidence model
        ↓
Storage/provider adapters ───── Platform authority adapters
        ↓                               ↓
Local / SAF / External / Drive     Sync / Backup / Everkeep
Network / other supported roots    Privacy / Wardveil / Identity / Mesh
```

Storage providers own filesystem/location mechanics. Platform authorities own their respective evidence and authorization domains. The application composes those results without transferring authority between them.

## 3. Provider contract

Every storage provider should eventually expose a capability declaration and operations appropriate to its authority. A provider that is read-only must not advertise mutation. A provider unable to verify a final operation state must return an uncertain/reconciliation state instead of declaring success.

The first concrete provider is deliberately narrow: app-private Android storage. It proves the native browsing path without requesting broad storage privileges before permission, policy, privacy, and security behavior is designed and tested.

## 4. File identity

A visible item must have a provider-scoped identity rather than relying only on a display path. Long-term provider identities should support exact operation targeting, conflict detection, state refresh, provenance, and safe cross-provider transfer.

## 5. Unified evidence model

File Manager composes separate evidence dimensions. At minimum:

```text
storage/location
local availability
sync state + evidence
backup/recoverability state + evidence
Everkeep continuity/preservation state + evidence
sharing/access
Privacy Shield state + evidence
Wardveil state + evidence
identity/device context
version/provenance/activity
```

No dimension can silently manufacture another. The UI must preserve `unknown`, `unavailable`, `pending`, `expired`, and `unverified` where applicable.

## 6. Sync versus backup

A synchronized object is a replicated working state. Synchronization can propagate deletion or corruption. Independent backup/recovery protection is a separate authority and evidence chain. Therefore:

```text
sync == synced  ≠  backup == verified recoverable
```

This invariant is represented in source and unit tests.

## 7. Platform authority boundaries

### GoreeCloud Drive

Drive is a storage/resource authority for its files, ownership and applicable sharing/version state. File Manager consumes Drive contracts; it does not pretend local paths are Drive resources.

### GoreeCloud Sync

Sync owns synchronization state, device replication/conflict state, and applicable synchronization controls.

### GoreeCloud Backup and Everkeep

Backup is the user-facing backup workflow/service. Everkeep is the broader continuity/resilience/preservation authority. File Manager must distinguish backup existence, integrity verification, restore testing, recovery eligibility, and preservation state.

### Privacy Shield

Privacy Shield determines privacy/consent/data-use authority. File Manager must not infer an ALLOW decision merely because an actor is authenticated. Indexing, remote search, previews, sharing, backup, synchronization, and telemetry are separate privacy-relevant operations.

### Wardveil Security

Wardveil determines applicable security evidence and security-policy outcomes. File Manager should consume normalized Wardveil contracts, never substitute a scanner-vendor result for Wardveil authority, and never label missing coverage as clean/protected.

### GoreeCloud Identity

Identity owns authentication, accounts, actors/services, credentials, sessions, devices, authorization primitives, and delegated authority relevant to file access.

### GoreeCloud Mesh

Mesh coordinates bounded events/state between systems. Successful delivery through Mesh is not proof that a resource operation or security/privacy/continuity decision succeeded.

### Glaze UI

Glaze UI governs application presentation, interaction, accessibility, responsiveness, and design-system semantics. It may visualize evidence but cannot create platform truth.

## 8. Operation architecture

Target mutation flow:

```text
user/app intent
→ resolve exact provider + resource identity
→ obtain applicable identity/privacy/security authorization/evidence
→ enqueue operation
→ provider executes or rejects
→ verify authoritative resulting state
→ reconcile if outcome certainty is lost
→ update activity/provenance and bounded Mesh events
```

High-impact operations should remain cancelable before commitment where technically possible and must not be blindly retried after an uncertain side effect.

## 9. Search architecture

Search will be layered: local/provider metadata search; authorized content indexing; cross-provider aggregation; saved filters/smart collections; and optional natural-language interpretation. Privacy Shield must gate collection/processing scope, and indexes must not become an uncontrolled copy of sensitive file content.

## 10. UI architecture

The current Android shell targets Glaze UI 2.0.0 semantics through application-local tokens and Material 3/Compose primitives. Current source includes a 48dp interaction-floor token and adaptive phone/wide navigation structure. This is an adoption foundation only; full rendered/native/accessibility/representative-device acceptance is pending.

## 11. Delivery phases

1. **Native foundation** — app shell, provider/evidence model, app-private browsing, CI.
2. **Local file manager** — authorized device roots, core operations, metadata, previews, operation queue, Trash strategy, search.
3. **GoreeCloud storage** — Drive + Sync integration, offline state, sharing/versions, cross-location transfers.
4. **Protection and continuity** — Wardveil, Privacy Shield, Backup/Everkeep, evidence-backed destructive-action safety and recovery.
5. **Cross-device intelligence** — Identity/Mesh device context, unified activity/provenance, smart/natural-language discovery.
6. **Product acceptance** — complete current Glaze/Wardveil/Privacy/Everkeep gates, runtime failure testing, representative-device acceptance, and Stable qualification.
