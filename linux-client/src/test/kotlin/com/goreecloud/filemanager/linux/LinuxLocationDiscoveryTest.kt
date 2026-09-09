package com.goreecloud.filemanager.linux

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LinuxLocationDiscoveryTest {
    @Test
    fun xdgDiscoveryExpandsOnlyHomeAndAbsoluteValuesWithoutExecutingShellText() {
        val home = Files.createTempDirectory("goreecloud-linux-home-")
        val config = Files.createDirectories(home.resolve("xdg-config"))
        val documents = Files.createDirectory(home.resolve("Documents"))
        val downloads = Files.createDirectory(home.resolve("Downloads"))
        val absolutePictures = Files.createTempDirectory("goreecloud-linux-pictures-")
        val userDirs = """
            XDG_DOCUMENTS_DIR="${'$'}HOME/Documents"
            XDG_DOWNLOAD_DIR="${'$'}{HOME}/Downloads"
            XDG_DESKTOP_DIR="${'$'}HOME"
            XDG_PICTURES_DIR="$absolutePictures"
            XDG_MUSIC_DIR="relative/Music"
            XDG_VIDEOS_DIR="${'$'}(touch /tmp/should-not-run)"
        """.trimIndent()

        val discovery = LinuxLocationDiscovery(
            homePath = home,
            environment = mapOf("XDG_CONFIG_HOME" to config.toString()),
            readText = { path -> if (path == config.resolve("user-dirs.dirs")) userDirs else null },
            mountInfoText = { null },
        )

        val candidates = discovery.discover()

        assertEquals(listOf("Home", "Documents", "Downloads", "Pictures"), candidates.map { it.displayName })
        assertEquals(home.toAbsolutePath().normalize(), candidates[0].path)
        assertTrue(candidates.any { it.path == documents.toAbsolutePath().normalize() })
        assertTrue(candidates.any { it.path == downloads.toAbsolutePath().normalize() })
        assertTrue(candidates.any { it.path == absolutePictures.toAbsolutePath().normalize() })
        assertTrue(candidates.all { it.requiresExplicitSelection })
        assertFalse(candidates.any { it.path.toString().contains("should-not-run") })
    }

    @Test
    fun relativeXdgConfigHomeFallsBackToHomeConfig() {
        val home = Files.createTempDirectory("goreecloud-linux-home-")
        val config = Files.createDirectories(home.resolve(".config"))
        val desktop = Files.createDirectory(home.resolve("Desktop"))
        val userDirs = "XDG_DESKTOP_DIR=\"${'$'}HOME/Desktop\""

        val discovery = LinuxLocationDiscovery(
            homePath = home,
            environment = mapOf("XDG_CONFIG_HOME" to "relative-config"),
            readText = { path -> if (path == config.resolve("user-dirs.dirs")) userDirs else null },
            mountInfoText = { null },
        )

        val candidates = discovery.discover()

        assertTrue(candidates.any { it.path == desktop.toAbsolutePath().normalize() })
    }

    @Test
    fun mountInfoDiscoveryDecodesEscapesAndUsesCandidateLanguageForRemovablePaths() {
        val mountInfo = """
            36 25 8:17 / /media/My\040Disk rw,relatime - vfat /dev/sdb1 rw
            37 25 0:45 / /mnt/team rw,relatime - cifs //server/team rw
            38 25 0:5 / /proc rw,nosuid,nodev,noexec - proc proc rw
            39 25 0:31 / /run/user/1000 rw,nosuid,nodev - tmpfs tmpfs rw
        """.trimIndent()

        val discovery = LinuxLocationDiscovery(
            homePath = Path.of("/home/test-user"),
            environment = emptyMap(),
            readText = { null },
            isDirectory = { true },
            mountInfoText = { mountInfo },
        )

        val candidates = discovery.discover()
        val removable = candidates.single { it.path == Path.of("/media/My Disk") }
        val network = candidates.single { it.path == Path.of("/mnt/team") }

        assertEquals(LinuxLocationCandidateKind.REMOVABLE_MEDIA_CANDIDATE, removable.kind)
        assertEquals("vfat", removable.filesystemType)
        assertEquals("/dev/sdb1", removable.filesystemSource)
        assertEquals(LinuxLocationCandidateKind.MOUNTED_FILESYSTEM, network.kind)
        assertEquals("cifs", network.filesystemType)
        assertFalse(candidates.any { it.path == Path.of("/proc") })
        assertTrue(candidates.all { it.requiresExplicitSelection })
    }

    @Test
    fun parsersIgnoreMalformedOrNonAbsoluteMountRecords() {
        val parsed = LinuxLocationDiscovery.parseMountInfo(
            """
                malformed
                40 25 0:45 / relative rw - ext4 /dev/test rw
                41 25 0:45 / /mnt/Good\011Name rw - ext4 /dev/test rw
            """.trimIndent(),
        )

        assertEquals(1, parsed.size)
        assertEquals(Path.of("/mnt/Good\tName"), parsed.single().mountPoint)
    }
}
