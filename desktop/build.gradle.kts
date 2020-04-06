plugins {
    kotlin("multiplatform") version "1.3.61"
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "12"
            }
        }
    }

    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir("src")
            dependencies {
                implementation(project(":core"))
                implementation(kotlin("stdlib-jdk8"))
                javaFXDependencies(listOf("base", "controls", "graphics"), "12").forEach {
                    implementation(it)
                }
                implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
            }
        }
    }
}

fun javaFXDependencies(modules: List<String>, version: String): List<String> {
    val currentOS = org.gradle.internal.os.OperatingSystem.current()
    val platform = currentOS.let {
        when {
            it.isWindows -> "win"
            it.isLinux   -> "linux"
            it.isMacOsX  -> "mac"
            else         -> ""
        }
    }

    return modules.map { "org.openjfx:javafx-${it}:${version}:${platform}" }
}