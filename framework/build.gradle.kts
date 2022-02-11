plugins {
    `java-library`
}

subprojects {
    apply {
        plugin("org.springframework.boot")
    }
    if (arrayOf("starter-logging", "config").contains(name)) {
        apply {
            plugin("org.jetbrains.kotlin.jvm")
            plugin("org.jetbrains.kotlin.plugin.spring")
            plugin("summer.kotlin-publish")
        }
    } else {
        apply {
            plugin("summer.publish")
        }
    }

    dependencies {
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks {
        "jar"(Jar::class) {
            enabled = true
            archiveClassifier.convention("")
        }
        "bootJar" { enabled = false }
    }
}
