import java.util.stream.Collectors

plugins {
    `java-library`
}

dependencies {
    api("org.springframework.boot:spring-boot-gradle-plugin")
    api("io.spring.gradle:dependency-management-plugin")
    api("org.atteo:evo-inflector")
    api(project(":plugin:generator-plugin"))
    api(project(":plugin:dist-plugin"))
    api(project(":plugin:profile-plugin"))
    api(project(":plugin:autodoc-plugin")) {
        exclude("ch.qos.logback", "logback-classic")
    }
    api("mysql:mysql-connector-java")
    api("com.oracle.database.jdbc:ojdbc8")
}

tasks {
    "jar"(Jar::class) {
        from(fileTree(mapOf("dir" to "libs")).files.stream().map { zipTree(it) }
            .collect(Collectors.toList()))
    }
    "processResources"(ProcessResources::class) {
        outputs.upToDateWhen { false }
        filesMatching(setOf("**/*.properties")) {
            filter(
                mapOf("tokens" to project.properties.filter { it.key.endsWith("version") }),
                org.apache.tools.ant.filters.ReplaceTokens::class.java
            )
        }
    }
}