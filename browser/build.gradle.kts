import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

apply {
    plugin("kotlin-dce-js")
}

val backEndDir by extra(projectDir.resolve("src/backEnd"))
val frontEndDir by extra(projectDir.resolve("src/frontEnd"))
val webDir by extra(
    System.getenv("WEB_DIR")?.let {
        File(it)
    } ?: projectDir.resolve("artifacts/web")
)
val devMode by extra((System.getenv("DEV_MODE") ?: "true").toBoolean())

kotlin {
    js {
        val main by compilations.getting {
            kotlinOptions {
                moduleKind = "umd"
            }
        }
    }

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

    sourceSets {
        val jsMain by getting {
            kotlin.srcDir(frontEndDir.resolve("js"))
            dependencies {
                implementation(project(":core"))
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
            }
        }

        val jvmMain by getting {
            kotlin.srcDir(backEndDir.resolve("jvm"))
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.ktor:ktor-server-netty:1.3.+")
                implementation("ch.qos.logback:logback-classic:1.2.+")
            }
        }
    }
}

tasks {
    register("assembleWeb") {
        dependsOn("assembleHtml", "assembleCss", "assembleJs")
    }

    register<Delete>("cleanWeb") {
        delete(webDir)
    }

    register<Copy>("assembleHtml") {
        from(frontEndDir.resolve("html"))
        into(webDir)
        doLast {
            webDir.listFiles()?.filter {
                it.name.endsWith(".html")
            }?.forEach {
                it.writeText("<!-- AUTOGENERATED FILE. DO NOT EDIT. -->\n${it.readText()}")
            }
        }
    }

    register<Copy>("assembleCss") {
        from(frontEndDir.resolve("css"))
        into(webDir.resolve("css"))
        doLast {
            webDir.resolve("css").listFiles()?.filter {
                it.name.endsWith(".css")
            }?.forEach {
                it.writeText("/* AUTOGENERATED FILE. DO NOT EDIT. */\n${it.readText()}")
            }
        }
    }

    register<Copy>("assembleJs") {
        dependsOn("jsMainClasses")

        if (devMode) {
            val jsJar = { regex: String ->
                configurations.getByName("jsRuntimeClasspath").single {
                    it.name.matches(Regex(regex))
                }
            }

            val stdLib = jsJar("kotlin-stdlib-js-.+\\.jar")
            val kotlinxHtml = jsJar("kotlinx-html-js-.+\\.jar")
            val tetrisCore = jsJar("core-js.jar")
            from(zipTree(stdLib), zipTree(kotlinxHtml), zipTree(tetrisCore), getByName("compileKotlinJs"))
            include { fileTreeElement ->
                val path = fileTreeElement.path
                (path.endsWith(".js") || path.endsWith(".js.map")) &&
                    (path.startsWith("META-INF/resources/") || !path.startsWith("META-INF/"))
            }
        } else {
            from(getByName("runDceJsKotlin"))
        }

        includeEmptyDirs = false
        into(webDir.resolve("js"))

        doLast {
            webDir.resolve("js").listFiles()?.filter {
                it.name.endsWith(".js")
            }?.forEach {
                it.writeText("/* AUTOGENERATED FILE. DO NOT EDIT. */\n${it.readText()}")
            }
        }
    }

    register<ShadowJar>("runnableServerJar") {
        manifest {
            attributes["Main-Class"] = "ServerKt"
        }
        archiveClassifier.set("all")
        val jvmMainCompilation = kotlin.jvm().compilations.getByName("main")
        from(jvmMainCompilation.output)
        configurations = mutableListOf(jvmMainCompilation.compileDependencyFiles as Configuration)
        archiveFileName.set("server.jar")
    }

    register<Exec>("runServer") {
        dependsOn("runnableServerJar", "assembleWeb")
        environment(mapOf("STATIC_FILES_DIR" to webDir.absolutePath))
        commandLine("java", "-jar", buildDir.resolve("libs/server.jar").absolutePath)
    }
}
