plugins {
    `java-library`
//    id("com.eriwen.gradle.js") version "2.14.1"
}

dependencies {
    api(project(":tools:tools"))
    api(project(":tools:generator"))
    api("org.jetbrains.kotlin:kotlin-reflect")

    //web
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-logging")
    api("com.github.ben-manes.caffeine:caffeine")
    api("com.squareup.okhttp3:okhttp")

    api("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("io.swagger:swagger-annotations")

    compileOnly("javax.mail:mail")

    compileOnly("net.logstash.logback:logstash-logback-encoder")
    testImplementation("net.logstash.logback:logstash-logback-encoder")

    compileOnly("org.springframework.boot:spring-boot-starter-websocket")
    testImplementation("org.springframework.boot:spring-boot-starter-websocket")

    compileOnly("com.github.axet:kaptcha")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    compileOnly("org.springframework.boot:spring-boot-starter-jdbc")
    compileOnly("org.springframework.boot:spring-boot-starter-data-redis")

    testImplementation("com.github.axet:kaptcha")
    testImplementation(project(":test"))
//    testImplementation(project(":natives:sap"))
//    testImplementation(project(":tools:weixin"))
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

//    "processResources" {
//        mustRunAfter("minifyJs")
//    }

//    "jar"(Jar::class) {
//        dependsOn("minifyJs")
//    }
}

