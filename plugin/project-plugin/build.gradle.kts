import java.util.stream.Collectors

plugins {
    `java-library`
    `kotlin-dsl`
}

dependencies {
    api("org.springframework.boot:spring-boot-gradle-plugin:2.2.5.RELEASE")
    api(project(":plugin:generator-plugin"))
    api(project(":plugin:dist-plugin"))
    api(project(":plugin:profile-plugin"))
    api(project(":plugin:autodoc-plugin")) {
        exclude("ch.qos.logback", "logback-classic")
    }
    api("mysql:mysql-connector-java")
    api("com.oracle.database.jdbc:ojdbc8:19.7.0.0")
    api(fileTree(mapOf("dir" to "libs")))
}

tasks {
    "jar"(Jar::class) {
        from(fileTree(mapOf("dir" to "libs")).files.stream().map { zipTree(it) }
            .collect(Collectors.toList()))
    }
}