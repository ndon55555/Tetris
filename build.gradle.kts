plugins {
    kotlin("multiplatform") version "1.5.10" apply false
}

allprojects {
    group = "com.github.ndon55555"

    tasks {
        withType<Wrapper> {
            gradleVersion = "7.0.2"
        }
    }
}

subprojects {
    repositories {
        jcenter()
        mavenCentral()
    }

    apply {
        plugin("org.jetbrains.kotlin.multiplatform")
    }

    dependencyLocking {
        lockAllConfigurations()
    }

    buildscript {
        configurations.classpath {
            resolutionStrategy.activateDependencyLocking()
        }
    }
}
