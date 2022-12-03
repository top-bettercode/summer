plugins {
    `java-library`
}

subprojects {

    apply {
        plugin("org.springframework.boot")
    }

    tasks {
        "jar"(Jar::class) {
            enabled = true
            archiveClassifier.convention("")
        }
        "bootJar" {
            enabled = false
        }
    }
}
