dependencies {
    compile(project(":util:autodoc-core"))

    //util
    compile(project(":util:api-sign"))
    compile(project(":util:generator"))

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    //test
    testCompile(kotlin("test-junit"))
    testCompile("org.springframework.boot:spring-boot-starter-test")
    testCompile("org.springframework.boot:spring-boot-starter-jdbc")
    testCompile("com.h2database:h2")
}