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
        val commonMain by getting {
            kotlin.srcDir("src/common")
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val commonTest by getting {
            kotlin.srcDir("test/common")
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsMain by getting {
            kotlin.srcDir("src/js")
            dependencies {
                dependsOn(commonMain)
                implementation(kotlin("stdlib-js"))
            }
        }

        val jvmMain by getting {
            kotlin.srcDir("src/jvm")
            dependencies {
                dependsOn(commonMain)
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            kotlin.srcDir("test/jvm")
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.5.+")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.+")
            }
        }
    }

    tasks {
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
    }
}