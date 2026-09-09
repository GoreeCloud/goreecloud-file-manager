package com.goreecloud.filemanager.linux

import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * Non-production Linux development harness.
 *
 * An explicit root performs a read-only listing through LinuxFileRepository. `--locations` performs
 * read-only XDG/mount candidate discovery without constructing a provider or granting file access.
 * This exists to validate Linux application/provider boundaries before a Glaze UI desktop shell and
 * accepted package are implemented.
 */
fun main(args: Array<String>) {
    if (args.size == 1 && args.single() == "--locations") {
        println("GoreeCloud File Manager — Linux development location discovery")
        println("Discovery only: candidates require explicit selection; no provider authorization is granted.")
        LinuxLocationDiscovery().discover().forEach { candidate ->
            val filesystem = candidate.filesystemType?.let { "\t$it" }.orEmpty()
            println("${candidate.kind}\t${candidate.displayName}\t${candidate.path}$filesystem")
        }
        return
    }

    if (args.size != 1) {
        System.err.println("Usage: linux-client <explicit-root-directory> | --locations")
        System.err.println("This development harness is read-only and is not the production Linux File Manager.")
        exitProcess(2)
    }

    val provider = runCatching { LinuxFileRepository(Path.of(args.single())) }
        .getOrElse {
            System.err.println("Unable to open the explicitly selected Linux root.")
            exitProcess(1)
        }

    println("GoreeCloud File Manager — Linux development harness")
    println("Selected root: ${provider.root.displayName}")
    println("Mode: read-only listing; production desktop UI and package acceptance are pending")

    provider.list(provider.root).forEach { entry ->
        println("${entry.type}\t${entry.displayName}")
    }
}
