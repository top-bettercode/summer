plugins { `java-library` }

dependencies {
    api(project(":framework:security-core"))
    api("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure")


    testImplementation(project(":util:test"))
}


