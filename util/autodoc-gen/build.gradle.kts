plugins {
    `java-library`
}

dependencies {
    api(project(":util:autodoc-core"))

    //util
    api(project(":util:api-sign"))
    api(project(":util:generator"))

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    //test
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("com.h2database:h2")
}