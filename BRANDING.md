# GoreeCloud File Manager Branding

Branding authority: `GoreeCloud/goreecloud-branding-assets`.

## Approved product identity

GoreeCloud File Manager now has approved canonical product artwork at:

`products/file-manager/app-icon.svg`

Canonical Git blob:

`c723a84eb2ecb29ef8a0cef845eb1d2cff714cd0`

The approved identity uses paired file/provider panes connected by a bidirectional management corridor. It is deliberately distinct from GoreeCloud Drive's folder identity and from generic folder artwork.

Source-level visual acceptance, small-size/grayscale review, ecosystem collision review, semantic-state review, and graphical-contrast evidence are retained in `GoreeCloud/goreecloud-branding-assets` under `concepts/product-identity-round-1/REVIEW-EVIDENCE.md`.

## Android derivative

This repository consumes a traceable native Android VectorDrawable derivative at:

`app/src/main/res/drawable/goreecloud_file_manager_icon.xml`

The application manifest consumes it through:

`android:icon="@drawable/goreecloud_file_manager_icon"`

The derivative preserves the canonical 64×64 geometry, teal→indigo identity relationship, white foreground construction, paired-pane silhouette, and bidirectional management motif within Android VectorDrawable constraints.

This direct vector launcher derivative is the current Android packaging role. Adaptive/monochrome launcher variants may be added when required by an accepted platform-specific derivative specification; they must remain traceable to the same canonical source rather than becoming independent artwork.

## Identity boundary

File Manager represents provider-spanning file browsing, organization, and control across supported local, cloud, synchronized, removable, network, backup, and continuity contexts. The identity does not assert that any particular provider, operation, synchronization path, backup/recovery path, privacy state, security state, or continuity state is currently available or successful.

Operational state remains separate from product branding and must use evidence-backed Glaze UI semantics.

## Consumer-derivative rule

This repository is a branding consumer, not the canonical branding authority. Repository, release, web, Android, or other platform copies are derivatives of the exact canonical source above and must remain traceable to its approved Git blob.

Branding acceptance does not establish application production acceptance, controlled production signing/deployment, representative-device acceptance, complete Glaze UI consumer conformance, or Stable qualification.
