plugins { `java-library` }

dependencies {
    api(project(":util:common-lang"))

    api("org.springframework.boot:spring-boot-starter-logging")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.springframework.boot:spring-boot-starter-web")
    api("com.google.guava:guava")

    api("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("javax.mail:mail")
    compileOnly("net.logstash.logback:logstash-logback-encoder")
    testImplementation("net.logstash.logback:logstash-logback-encoder")

    compileOnly("org.springframework.boot:spring-boot-starter-websocket")
    testImplementation("org.springframework.boot:spring-boot-starter-websocket")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}
