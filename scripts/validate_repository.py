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
    "BRANDING.md",
    "USER-MANUAL.md",
    "SECURITY.md",
    ".gitignore",
    ".editorconfig",
    "goreecloud.platform.yaml",
    "settings.gradle.kts",
    "build.gradle.kts",
    "app/build.gradle.kts",
    "core/build.gradle.kts",
    "linux-client/build.gradle.kts",
    ".github/workflows/android.yml",
    ".github/workflows/linux.yml",
]
required_source = [
    "app/src/main/AndroidManifest.xml",
    "app/src/main/res/drawable/goreecloud_file_manager_icon.xml",
    "app/src/main/java/com/goreecloud/filemanager/MainActivity.kt",
    "app/src/main/java/com/goreecloud/filemanager/storage/LocalFileRepository.kt",
    "app/src/main/java/com/goreecloud/filemanager/storage/SafTreeFileRepository.kt",
    "app/src/main/java/com/goreecloud/filemanager/ui/FileManagerApp.kt",
    "app/src/test/java/com/goreecloud/filemanager/storage/LocalFileRepositoryTest.kt",
    "core/src/main/kotlin/com/goreecloud/filemanager/model/FileModels.kt",
    "core/src/main/kotlin/com/goreecloud/filemanager/platform/PlatformAuthorities.kt",
    "core/src/main/kotlin/com/goreecloud/filemanager/storage/FileStorageProvider.kt",
    "core/src/main/kotlin/com/goreecloud/filemanager/storage/FileTransferService.kt",
    "core/src/test/kotlin/com/goreecloud/filemanager/model/FileStatusTest.kt",
    "core/src/test/kotlin/com/goreecloud/filemanager/storage/FileTransferServiceTest.kt",
    "linux-client/src/main/kotlin/com/goreecloud/filemanager/linux/LinuxFileRepository.kt",
    "linux-client/src/main/kotlin/com/goreecloud/filemanager/linux/LinuxDevelopmentMain.kt",
    "linux-client/src/test/kotlin/com/goreecloud/filemanager/linux/LinuxFileRepositoryTest.kt",
]

errors = []
for relative in required_root + required_source:
    if not (ROOT / relative).is_file():
        errors.append(f"missing required file: {relative}")

settings = (ROOT / "settings.gradle.kts").read_text(encoding="utf-8")
if 'include(":app", ":core", ":linux-client")' not in settings:
    errors.append("settings.gradle.kts must include :app, :core, and :linux-client")

app_gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
if 'implementation(project(":core"))' not in app_gradle:
    errors.append("Android app must depend on the shared :core module")

core_gradle = (ROOT / "core/build.gradle.kts").read_text(encoding="utf-8")
for required_text in [
    'id("org.jetbrains.kotlin.jvm")',
    "jvmToolchain(17)",
]:
    if required_text not in core_gradle:
        errors.append(f"core/build.gradle.kts missing shared-core build requirement: {required_text!r}")

linux_gradle = (ROOT / "linux-client/build.gradle.kts").read_text(encoding="utf-8")
for required_text in [
    "application",
    'id("org.jetbrains.kotlin.jvm")',
    'implementation(project(":core"))',
    "jvmToolchain(17)",
    'mainClass.set("com.goreecloud.filemanager.linux.LinuxDevelopmentMainKt")',
]:
    if required_text not in linux_gradle:
        errors.append(f"linux-client/build.gradle.kts missing Linux development build requirement: {required_text!r}")

android_workflow = (ROOT / ".github/workflows/android.yml").read_text(encoding="utf-8")
for required_text in [
    "github.event.pull_request.head.sha || github.sha",
    "gradle :core:test :app:testDebugUnitTest",
    "gradle :app:lintDebug",
    "gradle :app:assembleDebug",
]:
    if required_text not in android_workflow:
        errors.append(f"Android workflow missing exact/shared validation requirement: {required_text!r}")

linux_workflow = (ROOT / ".github/workflows/linux.yml").read_text(encoding="utf-8")
for required_text in [
    "github.event.pull_request.head.sha || github.sha",
    "gradle :core:test :linux-client:test",
    "gradle :linux-client:installDist :linux-client:distTar",
    "Smoke-test explicit-root development harness",
    "Development JVM distribution only; not an accepted Linux desktop package or Stable release.",
]:
    if required_text not in linux_workflow:
        errors.append(f"Linux workflow missing development validation boundary: {required_text!r}")

readme = (ROOT / "README.md").read_text(encoding="utf-8")
for required_text in [
    "GLAZE UI V1.3 / 1.3.0",
    "Linux and Android",
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

for pattern in [
    r"\bis protected by wardveil\b",
    r"\bwardveil[- ]protected\b",
    r"\bfully protected by wardveil\b",
]:
    if re.search(pattern, readme, flags=re.IGNORECASE):
        errors.append("README contains an unverified positive Wardveil protection claim")
        break

branding = (ROOT / "BRANDING.md").read_text(encoding="utf-8")
for required_text in [
    "GoreeCloud/goreecloud-branding-assets",
    "products/file-manager/app-icon.svg",
    "c723a84eb2ecb29ef8a0cef845eb1d2cff714cd0",
    "app/src/main/res/drawable/goreecloud_file_manager_icon.xml",
    'android:icon="@drawable/goreecloud_file_manager_icon"',
    "GoreeCloud Drive",
    "approved canonical product artwork",
]:
    if required_text not in branding:
        errors.append(f"BRANDING.md missing approved identity provenance: {required_text!r}")

manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
if 'android:icon="@drawable/goreecloud_file_manager_icon"' not in manifest:
    errors.append("Android manifest does not consume the approved File Manager icon derivative")

icon = (ROOT / "app/src/main/res/drawable/goreecloud_file_manager_icon.xml").read_text(encoding="utf-8")
for required_text in [
    '#0D9488',
    '#4F46E5',
    'android:viewportWidth="64"',
    'android:viewportHeight="64"',
    'M17.5,16H23.5',
    'M40.5,16H46.5',
    'M27,27H37',
    'M37,37H27',
]:
    if required_text not in icon:
        errors.append(f"Android File Manager icon derivative drifted: {required_text!r}")

architecture = (ROOT / "ARCHITECTURE.md").read_text(encoding="utf-8")
for required_text in [
    "Sync versus backup",
    "User-authorized Android document-tree provider",
    "recursive folder deletion",
    "Required Linux client architecture",
    "Cross-platform resource identity",
]:
    if required_text.lower() not in architecture.lower():
        errors.append(f"ARCHITECTURE.md missing required architecture invariant: {required_text!r}")

specifications = (ROOT / "SPECIFICATIONS.md").read_text(encoding="utf-8")
for required_text in [
    "Required native platforms",
    "Linux and Android",
    "Linux implementation status",
    "Cross-platform resource identity",
    "GLAZE UI V1.3 / 1.3.0",
]:
    if required_text not in specifications:
        errors.append(f"SPECIFICATIONS.md missing required platform baseline: {required_text!r}")

models = (ROOT / "core/src/main/kotlin/com/goreecloud/filemanager/model/FileModels.kt").read_text(encoding="utf-8")
for token in [
    "SyncState",
    "BackupState",
    "PrivacyState",
    "SecurityState",
    "EvidenceState",
    "FileCapability",
    "FileOperationOutcome",
    "StorageProviderDescriptor",
    "SYMLINK",
]:
    if token not in models:
        errors.append(f"shared core model missing {token}")

provider_contract = (ROOT / "core/src/main/kotlin/com/goreecloud/filemanager/storage/FileStorageProvider.kt").read_text(encoding="utf-8")
for required_text in [
    "interface FileStorageProvider",
    "object FileNamePolicy",
    "fun FileEntry.asBrowserLocation",
]:
    if required_text not in provider_contract:
        errors.append(f"shared core provider contract missing {required_text!r}")

transfer_service = (ROOT / "core/src/main/kotlin/com/goreecloud/filemanager/storage/FileTransferService.kt").read_text(encoding="utf-8")
for required_text in [
    'MessageDigest.getInstance("SHA-256")',
    "if (!deleteSource)",
    "val delete = sourceProvider.delete(source)",
    "Both files were kept.",
]:
    if required_text not in transfer_service:
        errors.append(f"shared transfer service missing safety requirement: {required_text!r}")

linux_provider = (ROOT / "linux-client/src/main/kotlin/com/goreecloud/filemanager/linux/LinuxFileRepository.kt").read_text(encoding="utf-8")
for required_text in [
    "LinkOption.NOFOLLOW_LINKS",
    "FileItemType.SYMLINK",
    "Resource escapes the selected Linux root.",
    "Symbolic-link traversal is not enabled.",
    "Mutations across a mount boundary are not enabled.",
    "Recursive folder deletion is not enabled.",
    "sameFileStore",
]:
    if required_text not in linux_provider:
        errors.append(f"Linux provider missing filesystem safety requirement: {required_text!r}")

linux_harness = (ROOT / "linux-client/src/main/kotlin/com/goreecloud/filemanager/linux/LinuxDevelopmentMain.kt").read_text(encoding="utf-8")
for required_text in [
    "Non-production Linux development harness",
    "explicit-root-directory",
    "production desktop UI and package acceptance are pending",
]:
    if required_text not in linux_harness:
        errors.append(f"Linux development harness missing truthful boundary: {required_text!r}")

linux_tests = (ROOT / "linux-client/src/test/kotlin/com/goreecloud/filemanager/linux/LinuxFileRepositoryTest.kt").read_text(encoding="utf-8")
for required_text in [
    "listShowsSymlinkWithoutGrantingTraversalCapabilities",
    "listRejectsProviderRelativeTraversalOutsideSelectedRoot",
    "createRenameAndDeleteRemainNonRecursive",
    "sharedTransferCopiesAndMovesBetweenLinuxProvidersWithIntegrityVerification",
]:
    if required_text not in linux_tests:
        errors.append(f"Linux provider tests missing safety/transfer case: {required_text!r}")

manual = (ROOT / "USER-MANUAL.md").read_text(encoding="utf-8")
for required_text in [
    "Adding an Android storage location",
    "Creating folders",
    "Renaming",
    "Deleting files and folders",
    "not Stable or production accepted",
    "Linux and Android",
]:
    if required_text.lower() not in manual.lower():
        errors.append(f"USER-MANUAL.md missing current user behavior: {required_text!r}")

platform_contract = (ROOT / "goreecloud.platform.yaml").read_text(encoding="utf-8")
for required_text in [
    'glaze_ui_required: "1.3.0"',
    'glaze-ui==1.3.0',
    'supported_platforms:\n  - android',
    'Linux is a required first-class File Manager target',
]:
    if required_text not in platform_contract:
        errors.append(f"goreecloud.platform.yaml missing current platform truth: {required_text!r}")

if errors:
    for error in errors:
        print(f"ERROR: {error}", file=sys.stderr)
    raise SystemExit(1)

print("GoreeCloud File Manager repository validation passed.")
