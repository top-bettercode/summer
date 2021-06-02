plugins { `java-library` }

dependencies {
    api("org.springframework.boot:spring-boot")
    api("org.slf4j:slf4j-api")
    api("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("org.springframework.cloud:spring-cloud-starter-config")
}
