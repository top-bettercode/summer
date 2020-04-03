plugins { `java-library` }
dependencies {
    api(project(":framework:web"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.hateoas:spring-hateoas")
    api("org.springframework.boot:spring-boot-starter-security")
    compileOnly("org.springframework.security.oauth:spring-security-oauth2")

    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    testImplementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure")
    testImplementation("org.springframework.security.oauth:spring-security-oauth2")
}


