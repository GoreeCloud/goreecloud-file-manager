#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]

required_files = [
    "linux-desktop/build.gradle.kts",
    "linux-desktop/src/main/kotlin/com/goreecloud/filemanager/linux/LinuxDesktopDevelopmentMain.kt",
    "linux-client/src/main/kotlin/com/goreecloud/filemanager/linux/LinuxDesktopController.kt",
    "linux-client/src/test/kotlin/com/goreecloud/filemanager/linux/LinuxDesktopControllerTest.kt",
    "FEATURE-ROADMAP.md",
    "README.md",
    "CONFORMANCE.md",
    "USER-MANUAL.md",
    "goreecloud.platform.yaml",
]

errors = []
for relative in required_files:
    if not (ROOT / relative).is_file():
        errors.append(f"missing Linux desktop development contract file: {relative}")


def require_text(relative: str, required: list[str]) -> None:
    path = ROOT / relative
    if not path.is_file():
        return
    text = path.read_text(encoding="utf-8")
    for token in required:
        if token not in text:
            errors.append(f"{relative} missing Linux desktop development requirement: {token!r}")


require_text(
    "build.gradle.kts",
    ['id("org.jetbrains.compose") version "1.12.0" apply false'],
)
require_text(
    "settings.gradle.kts",
    ['include(":linux-desktop")'],
)
require_text(
    "linux-desktop/build.gradle.kts",
    [
        'id("org.jetbrains.kotlin.jvm")',
        'id("org.jetbrains.kotlin.plugin.compose")',
        'id("org.jetbrains.compose")',
        'implementation(project(":core"))',
        'implementation(project(":linux-client"))',
        "implementation(compose.desktop.currentOs)",
        'implementation("org.jetbrains.compose.material3:material3:1.12.0-alpha03")',
        'tasks.register<JavaExec>("runDevelopment")',
        'LinuxDesktopDevelopmentMainKt',
    ],
)
require_text(
    "linux-desktop/src/main/kotlin/com/goreecloud/filemanager/linux/LinuxDesktopDevelopmentMain.kt",
    [
        "Non-production Linux desktop development surface",
        "Linux Development · GLAZE UI V1.3 target · conformance pending",
        "Discovery is not authorization",
        "controller.openHighlightedCandidate()",
        "read-only desktop browsing",
        "Existing provider mutation primitives are intentionally not surfaced",
    ],
)
require_text(
    "linux-client/src/main/kotlin/com/goreecloud/filemanager/linux/LinuxDesktopController.kt",
    [
        "Discovery and provider authorization remain separate by construction",
        "fun highlightCandidate",
        "fun openHighlightedCandidate",
        "val openedProvider = providerFactory(candidate.path)",
        "entry.providerId != currentProvider.descriptor.id",
        "fun returnToLocations",
    ],
)
require_text(
    "linux-client/src/test/kotlin/com/goreecloud/filemanager/linux/LinuxDesktopControllerTest.kt",
    [
        "discoveryAndHighlightDoNotConstructProvider",
        "explicitOpenConstructsProviderAndListsRoot",
        "folderNavigationStaysInsideAuthorizedProviderAndSupportsBack",
        "failedExplicitOpenDoesNotLeaveProviderAuthorized",
        "returningToLocationsClosesDesktopProviderBoundary",
    ],
)
require_text(
    ".github/workflows/linux.yml",
    [
        "python3 scripts/validate_linux_desktop.py",
        "gradle :linux-desktop:classes",
        "Desktop presentation-module compilation is development evidence only",
        "gradle :linux-client:installDist :linux-client:distTar",
    ],
)
require_text(
    "README.md",
    [
        "Compose Multiplatform Desktop `1.12.0`",
        "gradle :linux-desktop:runDevelopment",
        "only the explicit **Open location** action",
        "Current-Stable conformance is not claimed",
    ],
)
require_text(
    "CONFORMANCE.md",
    [
        "LinuxDesktopDevelopmentMain",
        "LinuxDesktopController",
        "only an explicit **Open location** action",
        "Compose Desktop development-source compilation",
    ],
)
require_text(
    "USER-MANUAL.md",
    [
        "Linux desktop development surface",
        "gradle :linux-desktop:runDevelopment",
        "Open location",
        "does not establish Linux Stable acceptance",
    ],
)
require_text(
    "FEATURE-ROADMAP.md",
    [
        "FR-013",
        "Native Linux desktop Glaze UI V1.3 surface",
        "supported_platforms",
    ],
)

platform = (ROOT / "goreecloud.platform.yaml").read_text(encoding="utf-8")
if "supported_platforms:\n  - android" not in platform:
    errors.append("goreecloud.platform.yaml must remain Android-only while the Linux desktop surface is development-only")
if "\n  - linux" in platform:
    errors.append("Linux must not be added to supported_platforms by the development desktop slice")

if errors:
    for error in errors:
        print(f"ERROR: {error}", file=sys.stderr)
    raise SystemExit(1)

print("GoreeCloud File Manager Linux desktop development validation passed.")
