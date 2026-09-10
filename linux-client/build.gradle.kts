plugins {
    application
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("com.goreecloud.filemanager.linux.LinuxDevelopmentMainKt")
}

tasks.register<JavaExec>("runDesktopDevelopment") {
    group = "application"
    description = "Runs the non-production GoreeCloud File Manager Linux desktop development surface."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.goreecloud.filemanager.linux.LinuxDesktopDevelopmentMainKt")
}
