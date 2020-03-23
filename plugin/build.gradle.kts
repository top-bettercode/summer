subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("cn.bestwu.plugin-publish")
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
        "dokkaJavadoc"(org.jetbrains.dokka.gradle.DokkaTask::class) {
            noStdlibLink = true
        }
        "compileJava" {
            dependsOn("processResources")
        }
        "bootJar" { enabled = false }
    }
}
