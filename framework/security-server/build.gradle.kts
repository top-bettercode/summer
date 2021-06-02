plugins { `java-library` }

dependencies {
    api(project(":framework:security-core"))
    api("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure")
    compileOnly("org.springframework.security:spring-security-rsa")


    testImplementation("org.springframework.security:spring-security-rsa")
    testImplementation(project(":util:test"))
}


