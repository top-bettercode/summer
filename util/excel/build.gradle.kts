dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    compile(project(":util:common-lang"))
    compile("javax.mail:mail")
    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.dhatim:fastexcel")
    compileOnly("org.dhatim:fastexcel-reader")
    testCompile("org.dhatim:fastexcel-reader")
    compileOnly(project(":framework:web"))
    testCompile(project(":framework:web"))
    testCompile("org.springframework.boot:spring-boot-starter-test")
}


