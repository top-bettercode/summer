import org.gradle.kotlin.dsl.*

//plugins {
//    id("com.eriwen.gradle.js") version "2.14.1"
//}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web")
    compile(kotlin("reflect"))

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    testCompile(kotlin("test-junit"))
    testCompile(project(":framework:starter-logging"))
    testCompile("org.springframework.boot:spring-boot-starter-test")
}

tasks {
//    "minifyJs"(MinifyJsTask::class) {
//        source(project.file("src/main/client/sign.js"))
//        setDest(project.file("src/main/resources/META-INF/_t/sign.min.js"))
//        closure {
//            warningLevel = "QUIET"
//            compilationLevel = "ADVANCED_OPTIMIZATION"
//        }
//    }
    "jar"(Jar::class) {
        enabled = true
//        dependsOn("minifyJs")
    }
    "processResources" {
//        mustRunAfter("minifyJs")
    }
}

