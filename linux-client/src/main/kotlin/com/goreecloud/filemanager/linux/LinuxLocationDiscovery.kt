package com.goreecloud.filemanager.linux

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

/**
 * Read-only Linux desktop location discovery.
 *
 * Discovery is intentionally separate from provider authorization. A discovered path is only a
 * candidate that may be presented to a user. File access still requires an explicit
 * LinuxFileRepository root selection; discovery never creates a provider or grants mutation.
 */
enum class LinuxLocationCandidateKind {
    HOME,
    XDG_USER_DIRECTORY,
    MOUNTED_FILESYSTEM,
    REMOVABLE_MEDIA_CANDIDATE,
}

data class LinuxLocationCandidate(
    val displayName: String,
    val path: Path,
    val kind: LinuxLocationCandidateKind,
    val source: String,
    val filesystemType: String? = null,
    val filesystemSource: String? = null,
    val requiresExplicitSelection: Boolean = true,
)

internal data class LinuxMountInfo(
    val mountPoint: Path,
    val filesystemType: String,
    val filesystemSource: String,
)

class LinuxLocationDiscovery(
    private val homePath: Path = Path.of(System.getProperty("user.home")),
    private val environment: Map<String, String> = System.getenv(),
    private val readText: (Path) -> String? = { path ->
        runCatching { Files.readString(path) }.getOrNull()
    },
    private val isDirectory: (Path) -> Boolean = { path ->
        Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)
    },
    private val mountInfoText: () -> String? = {
        runCatching { Files.readString(Path.of("/proc/self/mountinfo")) }.getOrNull()
    },
) {
    fun discover(): List<LinuxLocationCandidate> {
        val candidates = mutableListOf<LinuxLocationCandidate>()
        val home = homePath.toAbsolutePath().normalize()

        if (isDirectory(home)) {
            candidates += LinuxLocationCandidate(
                displayName = "Home",
                path = home,
                kind = LinuxLocationCandidateKind.HOME,
                source = "user.home",
            )
        }

        val configHome = resolveConfigHome(home)
        readText(configHome.resolve("user-dirs.dirs"))
            ?.let { parseUserDirectories(it, home) }
            ?.forEach { (key, path) ->
                if (isDirectory(path)) {
                    candidates += LinuxLocationCandidate(
                        displayName = XDG_LABELS.getValue(key),
                        path = path,
                        kind = LinuxLocationCandidateKind.XDG_USER_DIRECTORY,
                        source = "xdg:$key",
                    )
                }
            }

        mountInfoText()
            ?.let(::parseMountInfo)
            ?.filter { it.filesystemType !in PSEUDO_FILESYSTEMS }
            ?.filter { isUserFacingMount(it.mountPoint) }
            ?.filter { isDirectory(it.mountPoint) }
            ?.forEach { mount ->
                candidates += LinuxLocationCandidate(
                    displayName = mountDisplayName(mount.mountPoint),
                    path = mount.mountPoint,
                    kind = if (isRemovableMediaCandidate(mount.mountPoint)) {
                        LinuxLocationCandidateKind.REMOVABLE_MEDIA_CANDIDATE
                    } else {
                        LinuxLocationCandidateKind.MOUNTED_FILESYSTEM
                    },
                    source = "mountinfo",
                    filesystemType = mount.filesystemType,
                    filesystemSource = mount.filesystemSource,
                )
            }

        return candidates.distinctBy { it.path.toAbsolutePath().normalize().toString() }
    }

    private fun resolveConfigHome(home: Path): Path {
        val configured = environment["XDG_CONFIG_HOME"]?.trim().orEmpty()
        if (configured.isNotEmpty()) {
            val path = runCatching { Path.of(configured) }.getOrNull()
            if (path != null && path.isAbsolute) return path.normalize()
        }
        return home.resolve(".config").normalize()
    }

    companion object {
        private val XDG_LABELS = linkedMapOf(
            "XDG_DESKTOP_DIR" to "Desktop",
            "XDG_DOCUMENTS_DIR" to "Documents",
            "XDG_DOWNLOAD_DIR" to "Downloads",
            "XDG_MUSIC_DIR" to "Music",
            "XDG_PICTURES_DIR" to "Pictures",
            "XDG_PUBLICSHARE_DIR" to "Public Share",
            "XDG_TEMPLATES_DIR" to "Templates",
            "XDG_VIDEOS_DIR" to "Videos",
        )

        private val USER_DIRECTORY_LINE = Regex(
            """^\s*(XDG_(?:DESKTOP|DOCUMENTS|DOWNLOAD|MUSIC|PICTURES|PUBLICSHARE|TEMPLATES|VIDEOS)_DIR)\s*=\s*"([^"]*)"\s*(?:#.*)?$""",
        )

        private val MOUNT_ESCAPE = Regex("""\\([0-7]{3})""")

        private val PSEUDO_FILESYSTEMS = setOf(
            "proc",
            "sysfs",
            "devtmpfs",
            "devpts",
            "tmpfs",
            "cgroup",
            "cgroup2",
            "securityfs",
            "pstore",
            "debugfs",
            "tracefs",
            "mqueue",
            "hugetlbfs",
            "configfs",
            "fusectl",
            "ramfs",
            "autofs",
        )

        internal fun parseUserDirectories(text: String, home: Path): List<Pair<String, Path>> =
            text.lineSequence().mapNotNull { line ->
                val match = USER_DIRECTORY_LINE.matchEntire(line) ?: return@mapNotNull null
                val key = match.groupValues[1]
                val value = match.groupValues[2]
                val path = resolveUserDirectoryValue(value, home) ?: return@mapNotNull null
                key to path
            }.toList()

        internal fun parseMountInfo(text: String): List<LinuxMountInfo> =
            text.lineSequence().mapNotNull { line ->
                val separator = line.indexOf(" - ")
                if (separator < 0) return@mapNotNull null
                val left = line.substring(0, separator).trim().split(Regex("\\s+"))
                val right = line.substring(separator + 3).trim().split(Regex("\\s+"))
                if (left.size < 5 || right.size < 2) return@mapNotNull null

                val mountPoint = runCatching { Path.of(decodeMountToken(left[4])).normalize() }.getOrNull()
                    ?: return@mapNotNull null
                if (!mountPoint.isAbsolute) return@mapNotNull null

                LinuxMountInfo(
                    mountPoint = mountPoint,
                    filesystemType = right[0],
                    filesystemSource = decodeMountToken(right[1]),
                )
            }.toList()

        private fun resolveUserDirectoryValue(value: String, home: Path): Path? {
            if (value.isBlank() || value.contains('`') || value.contains("$(")) return null

            val candidate = when {
                value == "\$HOME" -> home
                value.startsWith("\$HOME/") -> home.resolve(value.removePrefix("\$HOME/") )
                value == "\${HOME}" -> home
                value.startsWith("\${HOME}/") -> home.resolve(value.removePrefix("\${HOME}/"))
                value.startsWith("/") && !value.contains('$') -> runCatching { Path.of(value) }.getOrNull()
                else -> null
            } ?: return null

            val normalized = candidate.toAbsolutePath().normalize()
            return normalized.takeIf { it.isAbsolute }
        }

        private fun decodeMountToken(value: String): String =
            MOUNT_ESCAPE.replace(value) { match ->
                match.groupValues[1].toInt(radix = 8).toChar().toString()
            }

        private fun isUserFacingMount(path: Path): Boolean {
            if (path == Path.of("/")) return false
            if (path.startsWith(Path.of("/proc"))) return false
            if (path.startsWith(Path.of("/sys"))) return false
            if (path.startsWith(Path.of("/dev"))) return false
            if (path.startsWith(Path.of("/boot"))) return false
            if (path.startsWith(Path.of("/snap"))) return false
            if (path.startsWith(Path.of("/run")) && !path.startsWith(Path.of("/run/media"))) return false
            return true
        }

        private fun isRemovableMediaCandidate(path: Path): Boolean =
            path.startsWith(Path.of("/media")) || path.startsWith(Path.of("/run/media"))

        private fun mountDisplayName(path: Path): String =
            path.fileName?.toString()?.takeIf { it.isNotBlank() } ?: path.toString()
    }
}
