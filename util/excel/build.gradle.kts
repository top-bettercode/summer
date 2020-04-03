plugins {
    `java-library`
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    api(project(":util:common-lang"))
    api("javax.mail:mail")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.dhatim:fastexcel")
    compileOnly("org.dhatim:fastexcel-reader")
    testImplementation("org.dhatim:fastexcel-reader")
    compileOnly(project(":framework:web"))
    testImplementation(project(":framework:web"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}


