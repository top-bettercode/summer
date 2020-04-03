plugins { `java-library` }
dependencies {
    api(project(":framework:security-core"))
    compileOnly("org.springframework.security.oauth:spring-security-oauth2")

    testImplementation(project(":framework:security-server"))
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    testImplementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure")
    testImplementation("org.springframework.security.oauth:spring-security-oauth2")
}


