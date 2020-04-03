plugins { `java-library` }
subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("summer.plugin-publish")
        plugin("com.jfrog.artifactory")
        plugin("org.springframework.boot")
    }
    dependencies{
        api(gradleApi())
        api(kotlin("stdlib"))

        testImplementation(kotlin("test-junit"))
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
