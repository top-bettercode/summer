dependencies {
    compile(project(":framework:web"))

    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.springframework.hateoas:spring-hateoas")
    compile("org.springframework.boot:spring-boot-starter-security")
    compileOnly("org.springframework.security.oauth:spring-security-oauth2")

    testCompile("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    testCompile("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure")
    testCompile("org.springframework.security.oauth:spring-security-oauth2")
}


