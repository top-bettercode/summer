plugins {
    `java-library`
}

dependencies {
    api(project(":web"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    compileOnly("org.bouncycastle:bcprov-jdk18on")
    compileOnly("org.springframework.boot:spring-boot-starter-data-redis")

    testImplementation(project(":test"))
    testImplementation("org.bouncycastle:bcprov-jdk18on")
}

