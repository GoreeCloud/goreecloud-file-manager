# GoreeCloud File Manager — Feature Roadmap

**Status:** Active roadmap control  
**Required native platforms:** Linux and Android  
**Authoritative project record:** `GoreeCloud/Projects/Project Specification — File Manager`  
**Canonical repository:** `GoreeCloud/goreecloud-file-manager`  
**Drive counterpart:** `GoreeCloud/Feature Roadmap/GoreeCloud File Manager/FEATURE-ROADMAP.docx`

This roadmap records active File Manager feature obligations and implementation status without replacing the authoritative project specification, repository implementation evidence, release gates, or GoreeCloud Tasks Management. A feature is not complete or Stable merely because it appears here.

The repository and Drive roadmap copies must remain materially synchronized. Status changes require authoritative implementation/validation evidence, and historical checkpoints must remain historically truthful.

## Status vocabulary

- **Validated development foundation** — merged source/build behavior has exact-revision validation but is not production/Stable acceptance.
- **Active** — implementation work is currently actionable or in progress.
- **Planned** — required/recommended work remains outstanding.
- **Blocked on prerequisite** — intentionally held until a stated safety/authority prerequisite exists.
- **Ongoing control** — governance obligation that remains continuously active.

## Roadmap

| ID | Feature / obligation | Priority | Current state |
| --- | --- | --- | --- |
| FR-001 | Reconcile every current planned or recommended File Manager feature against the authoritative project record and verified repository evidence. | High | Ongoing control |
| FR-002 | Move actionable feature obligations into GoreeCloud Tasks Management when required, preserving priority, dependency, and lifecycle disposition. | High | Ongoing control |
| FR-003 | Do not mark features implemented, complete, cancelled, or superseded without authoritative evidence and synchronized repository/Drive roadmap updates. | High | Ongoing control |
| FR-010 | Maintain Linux and Android as first-class native product targets without transferring one platform's storage identity, permissions, or acceptance evidence to the other. | High | Active; Android is the user-facing development client and Linux remains below supported-platform acceptance |
| FR-011 | Shared platform-neutral provider/resource/operation/evidence core with verified ordinary-file transfer semantics. | High | Validated development foundation on authoritative main `42c959099b606c42ded1dc649e59d96716beb233` |
| FR-012 | Linux desktop location discovery for Home/XDG user directories plus mounted-filesystem and removable-media candidates, while keeping discovery separate from explicit provider authorization. | High | Validated discovery foundation on authoritative implementation merge `1b6f34828d66c6f2869dda3b95429664f520de41`; Draft PR #18 adds a Development fail-closed refresh boundary so an opened provider is closed when its candidate disappears, mounted/removable identity changes, or rediscovery fails. Exact-head validation for PR #18 remains pending. |
| FR-013 | Native Linux desktop Glaze UI V1.3 surface with accessible desktop information architecture, context/action presentation, and current consumer conformance evidence. | High | Active Development candidate — Draft PR #16 adds explicit location selection and read-only browsing; stacked PR #17 exact corrected head `b86a9c880b9e8cf5154978b700949d7badfc990b` passed Linux Development `34526991854` and Android Foundation `34526991873`. Review/integration plus rendered Glaze/accessibility/device acceptance remain open. |
| FR-014 | Linux keyboard/pointer navigation, focus behavior, accessible names/state, screen-reader behavior, reduced-motion/scaling, and drag alternatives. | High | Planned |
| FR-015 | Linux file associations, Open With, reveal/open handoff, and desktop integration. | Medium | Planned |
| FR-016 | Linux clipboard file operations and drag-and-drop with provider-aware identity, capability, conflict, and recovery semantics. | High | Planned |
| FR-017 | Linux windows, tabs, context menus, split/dual-pane workflows, workspace restoration, and desktop density. | Medium | Planned |
| FR-018 | Supported Linux package/distribution matrix, dependency review, signing, release/rollback controls, and representative runtime acceptance. | High | Blocked on native desktop/runtime acceptance |
| FR-020 | Android user-facing destination-selection copy/move workflows over the verified shared transfer service, with explicit conflicts and reconciliation. | High | Planned |
| FR-021 | Multi-selection and durable/reconcilable Operations Center for file operations and transfers. | High | Planned |
| FR-022 | Unified Trash/recovery plus Backup/Everkeep evidence and destructive-operation safeguards before recursive deletion or recursive folder transfer. | High | Blocked on recovery/continuity prerequisites |
| FR-023 | GoreeCloud Drive and GoreeCloud Sync runtime providers with provider-scoped identity, offline/conflict state, and verified transfers. | High | Planned |
| FR-024 | Runtime Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Identity, GoreeCloud Mesh, and applicable GoreeCloud Manager integration under independent authority boundaries. | High | Planned |
| FR-025 | Search/indexing, previews, tags, collections, sharing, file details, provenance, storage intelligence, and smart discovery under applicable privacy/security authority. | Medium | Planned |
| FR-026 | Representative Android and Linux device/provider/distribution/accessibility/input/performance validation tied to exact release revisions. | High | Planned |

## Current acceptance boundary

`goreecloud.platform.yaml` remains Android-only in `supported_platforms`. The Linux shared-core/provider/build foundation is real and validated, and a native Linux desktop Development surface now exists as an unmerged candidate. Linux still requires governed review/integration of that surface, system integration, supported packaging, representative rendered/runtime/accessibility evidence, Platform-System acceptance, signing/release controls, and explicit lifecycle qualification before it may be represented as a currently supported or Stable platform.
