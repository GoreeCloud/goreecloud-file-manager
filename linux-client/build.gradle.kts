plugins {
    application
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("com.goreecloud.filemanager.linux.LinuxDevelopmentMainKt")
}
