package com.goreecloud.filemanager.linux

import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * Non-production Linux development harness.
 *
 * It accepts exactly one explicit filesystem root and performs a read-only listing through the
 * Linux provider. This exists to validate the Linux application/provider boundary and packaged JVM
 * distribution before a Glaze UI desktop shell is selected and accepted.
 */
fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: linux-client <explicit-root-directory>")
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
