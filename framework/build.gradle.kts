plugins {
    `java-library`
}

subprojects {
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
}
