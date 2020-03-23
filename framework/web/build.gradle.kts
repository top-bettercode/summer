dependencies {
    compile("cn.bestwu:starter-logging")

    //web
    compile("org.springframework.boot:spring-boot-starter-web")

    compileOnly("com.github.axet:kaptcha")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    compileOnly("org.springframework.boot:spring-boot-starter-jdbc")

    testCompile("com.github.axet:kaptcha")
}


