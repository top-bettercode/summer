plugins {
    `java-library`
//    id("com.eriwen.gradle.js") version "2.14.1"
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(project(":framework:starter-logging"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
//        dependsOn("minifyJs")
    }
    "processResources" {
//        mustRunAfter("minifyJs")
    }
}

