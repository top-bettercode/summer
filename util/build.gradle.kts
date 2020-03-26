subprojects {
    apply {
        plugin("com.jfrog.artifactory")
        plugin("org.springframework.boot")
    }
    if (arrayOf("excel").contains(name)) {
        apply {
            plugin("summer.publish")
        }
    } else {
        apply {
            plugin("org.jetbrains.kotlin.jvm")
            plugin("org.jetbrains.kotlin.plugin.spring")
            plugin("summer.kotlin-publish")
        }
    }
    tasks {
        "jar"(Jar::class) {
            enabled = true
        }
        "compileJava" {
            dependsOn("processResources")
        }
        "bootJar" { enabled = false }
    }
}
