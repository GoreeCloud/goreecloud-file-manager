#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]

required_root = [
    "README.md",
    "SPECIFICATIONS.md",
    "FEATURES.md",
    "BENEFITS.md",
    "COMPETITIVE-OBJECTIVES.md",
    "ARCHITECTURE.md",
    "CONFORMANCE.md",
    "USER-MANUAL.md",
]
required_source = [
    "app/src/main/AndroidManifest.xml",
    "app/src/main/java/com/goreecloud/filemanager/MainActivity.kt",
    "app/src/main/java/com/goreecloud/filemanager/model/FileModels.kt",
    "app/src/main/java/com/goreecloud/filemanager/platform/PlatformAuthorities.kt",
    "app/src/main/java/com/goreecloud/filemanager/storage/FileStorageProvider.kt",
    "app/src/main/java/com/goreecloud/filemanager/storage/LocalFileRepository.kt",
    "app/src/main/java/com/goreecloud/filemanager/storage/SafTreeFileRepository.kt",
    "app/src/main/java/com/goreecloud/filemanager/ui/FileManagerApp.kt",
]

errors = []
for relative in required_root + required_source:
    if not (ROOT / relative).is_file():
        errors.append(f"missing required file: {relative}")

readme = (ROOT / "README.md").read_text(encoding="utf-8")
for required_text in [
    "Glaze UI 2.0.0",
    "Wardveil Security",
    "Privacy Shield",
    "Everkeep",
    "not Stable or production accepted",
    "user-authorized Android document trees",
    "persisted URI permissions",
]:
    if required_text not in readme:
        errors.append(f"README missing required current-state text: {required_text!r}")

for misleading in [
    "hundreds of built-in features",
    "production ready",
]:
    if misleading.lower() in readme.lower():
        errors.append(f"README contains prohibited/unverified broad claim: {misleading!r}")

# Reject positive Wardveil protection claims while allowing explicit statements that
# such a claim is not currently justified.
for pattern in [
    r"\bis protected by wardveil\b",
    r"\bwardveil[- ]protected\b",
    r"\bfully protected by wardveil\b",
]:
    if re.search(pattern, readme, flags=re.IGNORECASE):
        errors.append("README contains an unverified positive Wardveil protection claim")
        break

architecture = (ROOT / "ARCHITECTURE.md").read_text(encoding="utf-8")
for required_text in [
    "Sync versus backup",
    "User-authorized Android document-tree provider",
    "Recursive folder deletion",
]:
    if required_text not in architecture:
        errors.append(f"ARCHITECTURE.md missing required architecture invariant: {required_text!r}")

models = (ROOT / "app/src/main/java/com/goreecloud/filemanager/model/FileModels.kt").read_text(encoding="utf-8")
for token in [
    "SyncState",
    "BackupState",
    "PrivacyState",
    "SecurityState",
    "EvidenceState",
    "FileCapability",
    "FileOperationOutcome",
    "StorageProviderDescriptor",
]:
    if token not in models:
        errors.append(f"unified model missing {token}")

manual = (ROOT / "USER-MANUAL.md").read_text(encoding="utf-8")
for required_text in [
    "Adding an Android storage location",
    "Creating folders",
    "Renaming",
    "Deleting files and folders",
    "not Stable or production accepted",
]:
    if required_text not in manual:
        errors.append(f"USER-MANUAL.md missing current user behavior: {required_text!r}")

if errors:
    for error in errors:
        print(f"ERROR: {error}", file=sys.stderr)
    raise SystemExit(1)

print("GoreeCloud File Manager repository validation passed.")
