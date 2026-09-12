# GoreeCloud File Manager — Notes

**Lifecycle:** Development  
**Last reconciled:** September 10, 2026

This file records implementation notes that must remain subordinate to the repository specifications, feature roadmap, conformance evidence, governing GoreeCloud instructions, and verified runtime state.

## Current verified development state

- Android and Linux are the intended first-party platform targets.
- The current Linux desktop branch exposes a genuine native Compose Desktop development surface for location discovery, explicit provider opening, read-only directory browsing, back navigation, refresh, contextual status, and inspector information.
- Linux location discovery and highlighting do not create filesystem authority. A bounded provider is created only after the user explicitly opens the highlighted location.
- The Linux desktop surface intentionally does not expose mutation operations yet even where lower-level provider primitives exist.
- The current Linux desktop work is Development evidence only. It is not Linux Stable support, supported packaging, complete Glaze UI acceptance, mount-lifecycle acceptance, safe-eject acceptance, or production readiness.

## Open acceptance work

Physical Linux validation, keyboard and assistive-technology acceptance, complete current-Stable Glaze UI conformance, mount/removable-media lifecycle behavior, safe eject, mutation/recovery UX, packaging/distribution, Wardveil/Privacy Shield/Everkeep/Identity/Mesh/Manager acceptance, and release evidence remain separate obligations.

## Documentation rule

Do not use this notes file to promote a feature or platform state. Update `FEATURE-ROADMAP.md`, conformance records, governing Drive records, and Tasks Management when a material implementation or lifecycle state changes.
