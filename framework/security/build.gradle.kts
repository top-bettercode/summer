plugins { `java-library` }

dependencies {
    api(project(":framework:web"))
    api("org.springframework.boot:spring-boot-starter-security")
    compileOnly("org.springframework.data:spring-data-redis")
    compileOnly("org.springframework.boot:spring-boot-starter-jdbc")

    testImplementation(project(":util:test"))
}


