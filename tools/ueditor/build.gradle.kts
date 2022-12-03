plugins {
    `java-library`
}

apply {
    plugin("summer.publish")
}

dependencies {
    api(project(":tools:tools"))

    api("commons-codec:commons-codec")
    api("org.json:json")
    api("org.springframework:spring-core")

    compileOnly("javax.servlet:javax.servlet-api")
}
