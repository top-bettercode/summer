plugins { `java-library` }

dependencies {
    api(project(":framework:web"))
    api("org.springframework.boot:spring-boot-starter-security")

    compileOnly("org.springframework.boot:spring-boot-starter-data-redis")
    compileOnly("org.springframework.boot:spring-boot-starter-jdbc")

    testImplementation(project(":util:test"))

    testImplementation("org.springframework.boot:spring-boot-starter-data-redis")

    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("mysql:mysql-connector-java")
}


