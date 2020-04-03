plugins { `java-library` }
dependencies {
    api(kotlin("reflect"))

    api(project(":util:common-lang"))
    api("org.springframework.boot:spring-boot")
    api("org.springframework.boot:spring-boot-starter-logging")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.springframework.boot:spring-boot-starter-web")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("javax.mail:mail")
    compileOnly("net.logstash.logback:logstash-logback-encoder")
    testImplementation("net.logstash.logback:logstash-logback-encoder")

    compileOnly("org.springframework.boot:spring-boot-starter-websocket")
    testImplementation("org.springframework.boot:spring-boot-starter-websocket")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test-junit"))
}
