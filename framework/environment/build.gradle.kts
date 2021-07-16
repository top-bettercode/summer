plugins { `java-library` }

dependencies {
    api("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("org.springframework.cloud:spring-cloud-starter-config")
}
