subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("summer.plugin-publish")
        plugin("com.jfrog.artifactory")
        plugin("org.springframework.boot")
    }
    dependencies{
        compile(gradleApi())
        compile(kotlin("stdlib"))

        testCompile(kotlin("test-junit"))
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
