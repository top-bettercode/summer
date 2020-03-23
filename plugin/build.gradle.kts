subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("cn.bestwu.plugin-publish")
        plugin("com.jfrog.artifactory")
        plugin("org.springframework.boot")
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
