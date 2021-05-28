plugins { `java-library` }
dependencies {
    api(project(":framework:security-core"))
    api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    compileOnly(project(":framework:security-server"))

    testImplementation(project(":framework:security-server"))

}


