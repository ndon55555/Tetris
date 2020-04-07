plugins {
    kotlin("multiplatform") version "1.3.71" apply false
}

allprojects {
    group = "com.github.ndon55555"

    tasks {
        withType<Wrapper> {
            gradleVersion = "5.4.1"
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
}