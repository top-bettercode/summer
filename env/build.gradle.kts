plugins {
    `java-library`
}

apply {
    plugin("org.springframework.boot")
    plugin("summer.publish")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-actuator")
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
