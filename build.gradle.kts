import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.40"
    id("org.openjfx.javafxplugin") version "0.0.7"
}

group = "tetris"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:1.7.19")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
}

apply(plugin = "org.openjfx.javafxplugin")

javafx {
    modules("javafx.controls")
}

configure<JavaPluginConvention> {
    version = "12.01"
    sourceCompatibility = JavaVersion.VERSION_1_8
}

sourceSets["main"].java.srcDirs("src")
sourceSets["test"].java.srcDirs("test")

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "12"
}