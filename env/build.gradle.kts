plugins {
    `java-library`
}

dependencies {
    api(project(":tools:tools"))
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.eclipse.jgit:org.eclipse.jgit")
}
