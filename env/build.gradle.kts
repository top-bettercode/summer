plugins {
    `java-library`
}

apply {
    plugin("summer.publish")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-actuator")
}
