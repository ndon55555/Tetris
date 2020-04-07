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
}