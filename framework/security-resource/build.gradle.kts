plugins { `java-library` }
dependencies {
    api(project(":framework:security-core"))
    compileOnly("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    testImplementation(project(":framework:security-server"))

}


