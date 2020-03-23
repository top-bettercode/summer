dependencies {
    compile(project(":framework:security-core"))
    compileOnly("org.springframework.security.oauth:spring-security-oauth2")

    testCompile("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    testCompile("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure")
    testCompile("org.springframework.security.oauth:spring-security-oauth2")
    testCompile("com.h2database:h2")
}


