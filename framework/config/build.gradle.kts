plugins { `java-library` }

dependencies {
    api(project(":framework:web"))
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-actuator")

}
