plugins {
    `java-library`
}

dependencies {
    api(project(":tools:tools"))

    api("commons-codec:commons-codec")
    api("org.json:json")
    api("org.springframework:spring-core")

    compileOnly("jakarta.servlet:jakarta.servlet-api")
}
