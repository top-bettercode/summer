
dependencies {
    compile(kotlin("reflect"))

    compile(project(":util:common-lang"))
    compile("org.springframework.boot:spring-boot")
    compile("org.springframework.boot:spring-boot-starter-logging")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin")
    compile("org.springframework.boot:spring-boot-starter-web")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("javax.mail:mail")
    compileOnly("net.logstash.logback:logstash-logback-encoder")
    testCompile("net.logstash.logback:logstash-logback-encoder")

    compile("org.springframework.boot:spring-boot-starter-websocket")
    testCompile("org.springframework.boot:spring-boot-starter-websocket")

    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile(kotlin("test-junit"))
}
