plugins { `java-library` }

dependencies {
    api(project(":framework:security-core"))
    api("org.springframework.security:spring-security-oauth2-authorization-server")
//    compileOnly("org.springframework.security:spring-security-rsa")


    testImplementation(project(":util:test"))
}


