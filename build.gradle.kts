group = "com.github.ndon55555"
version = "1.0-SNAPSHOT"

val common by extra("src/common")
val browser by extra("src/browser")
val webDir by extra("${projectDir}/web")
val desktop by extra("src/desktop")

plugins {
    kotlin("multiplatform") version "1.3.61"
    id("com.gradle.build-scan") version "2.4"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "12"
            }
        }

        val test by compilations.getting {
            kotlinOptions {
                jvmTarget = "12"
            }
        }
    }

    js {
        val main by compilations.getting {
            kotlinOptions {
                sourceMap = true
                moduleKind = "umd"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(common)
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val jvmMain by getting {
            kotlin.srcDir(desktop)
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                javaFXDependencies(listOf("base", "controls", "graphics"), "12").forEach {
                    implementation(it)
                }
                implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1")
            }
        }

        val jsMain by getting {
            kotlin.srcDir("${browser}/js")
            dependencies {
                dependsOn(commonMain)
                implementation(kotlin("stdlib-js"))
            }
        }
    }

    tasks {
        withType<Wrapper> {
            gradleVersion = "5.4.1"
        }

        withType<Test> {
            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }

            testLogging {
                events = setOf(
                    org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT
                )
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }

        register("assembleWeb") {
            dependsOn("assembleHtml", "assembleJs")
        }

        register<Copy>("assembleHtml") {
            from(file("${browser}/html"))
            into(webDir)
        }

        register<Copy>("assembleJs") {
            dependsOn("jsMainClasses")

            val kotlinJSStdLibJar = configurations.getByName("jsCompileClasspath").single {
                it.name.matches(Regex("kotlin-stdlib-js-.+\\.jar"))
            }

            includeEmptyDirs = false
            from(zipTree(kotlinJSStdLibJar), getByName("compileKotlinJs"))
            include { fileTreeElement ->
                val path = fileTreeElement.path
                (path.endsWith(".js") || path.endsWith(".js.map")) &&
                    (path.startsWith("META-INF/resources/") || !path.startsWith("META-INF/"))
            }
            into(webDir)
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
