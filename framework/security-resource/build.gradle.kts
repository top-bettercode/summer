dependencies {
    compile(project(":framework:security-core"))
    compileOnly("org.springframework.security.oauth:spring-security-oauth2")

    testCompile(project(":framework:security-server"))
    testCompile("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    testCompile("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure")
    testCompile("org.springframework.security.oauth:spring-security-oauth2")
}


