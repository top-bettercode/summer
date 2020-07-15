plugins {
    `java-library`
}

dependencies {
    api(kotlin("stdlib"))
    api("com.fasterxml.jackson.core:jackson-databind")
    compileOnly("org.jsoup:jsoup")
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.jsoup:jsoup")
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

