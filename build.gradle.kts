import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gradle.build-scan") version "2.4"
    kotlin("jvm") version "1.3.40"
    id("org.openjfx.javafxplugin") version "0.0.7"
    id("com.dorongold.task-tree") version "1.4"
    jacoco
    application
}

group = "tetris"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1")
}

apply(plugin = "org.openjfx.javafxplugin")

javafx {
    version = "12"
    modules("javafx.controls")
}

jacoco {
    toolVersion = "0.8.4"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_12
}

sourceSets["main"].java.srcDirs("src")
sourceSets["test"].java.srcDirs("test")

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "12"
    }

    withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }

        testLogging {
            events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT
            )
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    register<Jar>("uberJar") {
        destinationDirectory.set(File("${project.rootDir}/artifacts"))

        manifest {
            attributes["Main-Class"] = "MainKt"
        }

        archiveClassifier.set("uber")
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)

        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }

    withType<Wrapper> {
        gradleVersion = "5.4.1"
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }

        dependsOn("test")
    }

    check {
        dependsOn("jacocoTestReport")
    }
}

application {
    mainClassName = "MainKt"
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}