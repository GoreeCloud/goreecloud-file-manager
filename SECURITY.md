# Security Policy

## Supported security state

GoreeCloud File Manager is under active development. The current implemented client is Android-only; Linux is a required first-class product target but has no accepted source/build/runtime implementation yet. Security fixes apply to the current controlled development source line and must pass the repository's applicable validation gates.

A source revision, development APK, Linux development artifact in the future, release artifact, or successful CI run must not be interpreted as production or Stable approval unless the separate GoreeCloud release and production-readiness records explicitly establish that status.

## Reporting a vulnerability

Do not publish active credentials, tokens, private keys, signing material, personal information, private file contents, production configuration, internal network details, or a working exploit containing sensitive GoreeCloud data in a public issue or pull request.

If a suspected vulnerability can be described safely without reusable secrets or private data, open a GitHub issue with only the information needed to reproduce and assess it. Mark the report clearly as a security concern and use synthetic files, accounts, paths, identifiers, and provider data wherever possible.

If safe public disclosure is not possible, do not place sensitive material in repository history. Use GitHub's private vulnerability-reporting capability when enabled for this repository, or contact the repository owner through an approved private channel listed on the GoreeCloud profile.

## What to include

A useful report identifies the affected File Manager revision and platform, the storage/provider type, the security boundary that failed, the operation performed, expected behavior, observed behavior, and a minimal reproduction using synthetic data. Include logs only after removing credentials, tokens, URI grants, private paths, personal information, file contents, and other unnecessary sensitive values.

## Security boundaries to preserve

Reports are especially important when they involve:

- path traversal or escape from an authorized filesystem/provider root;
- Android Storage Access Framework permission or document-tree boundary bypass;
- future Linux path, symbolic-link, mount, permission, or ownership boundary bypass;
- incorrect provider/resource identity or cross-provider authorization;
- unsafe copy, move, rename, delete, overwrite, recursive, or recovery behavior;
- transfer-integrity verification bypass or source deletion before destination verification;
- file association, Open With, drag-and-drop, archive, preview, or content-handling injection paths;
- unauthorized indexing, sharing, synchronization, backup, remote processing, or telemetry;
- false positive Wardveil, Privacy Shield, Everkeep, Identity, Mesh, synchronization, backup, or recovery state;
- credential, token, signing-material, private file-content, or sensitive metadata exposure;
- dependency, build, package, release, or provenance integrity failures;
- bypass of required repository, platform-contract, accessibility, release, or production-readiness gates.

## Authority boundaries

Operating-system file access does not itself establish Privacy Shield authorization for unrelated processing. A synchronized copy is not backup evidence. A backup record is not verified recoverability. Missing or stale Wardveil evidence is not a clean/protected result. GoreeCloud Identity authentication does not automatically broaden privacy or security authority. GoreeCloud Mesh coordination does not replace the systems that own file, privacy, security, identity, or continuity truth.

Security remediation must preserve these boundaries rather than bypass them for convenience.

## Response and remediation

Security findings that can affect confidentiality, integrity, authorization, file safety, recoverability, or release evidence should be addressed before unrelated feature expansion. Remediation should add or strengthen regression coverage and applicable platform validation. A failed security or safety gate is corrected rather than waived simply to make a pull request mergeable.

No production deployment, credential rotation, signing action, repository-secret change, infrastructure change, broad filesystem authorization, or other live administrative action is authorized by this policy itself.
