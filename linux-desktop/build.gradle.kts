plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":linux-client"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:1.12.0-alpha03")
}

tasks.register<JavaExec>("runDevelopment") {
    group = "application"
    description = "Runs the non-production GoreeCloud File Manager Linux desktop development surface."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.goreecloud.filemanager.linux.LinuxDesktopDevelopmentMainKt")
}
