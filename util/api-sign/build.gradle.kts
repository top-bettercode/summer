import com.eriwen.gradle.js.tasks.MinifyJsTask
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("com.eriwen.gradle.js") version "2.14.1"
}

apply {
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.jetbrains.kotlin.plugin.spring")

    plugin("cn.bestwu.kotlin-publish")
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web")
    compile(kotlin("reflect"))

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    testCompile(kotlin("test-junit"))
    testCompile("cn.bestwu:starter-logging:2.0.9")
    testCompile("org.springframework.boot:spring-boot-starter-test")
}

tasks {
    "minifyJs"(MinifyJsTask::class) {
        source(project.file("src/main/client/sign.js"))
        setDest(project.file("src/main/resources/META-INF/_t/sign.min.js"))
        closure {
            warningLevel = "QUIET"
            compilationLevel = "ADVANCED_OPTIMIZATION"
        }
    }
    "jar"(Jar::class) {
        enabled = true
        dependsOn("minifyJs")
    }
    "processResources" {
        mustRunAfter("minifyJs")
    }
    "dokkaJavadoc"(DokkaTask::class) {
        noStdlibLink = true
    }


}
