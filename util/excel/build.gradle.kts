dependencies {
    compile("cn.bestwu:common-lang")
    compile("javax.mail:mail")
    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.dhatim:fastexcel")
    compileOnly("org.dhatim:fastexcel-reader")
    testCompile("org.dhatim:fastexcel-reader")
    compileOnly(project(":framework:web"))
    testCompile(project(":framework:web"))
}


