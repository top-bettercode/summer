subprojects {
    apply {
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
