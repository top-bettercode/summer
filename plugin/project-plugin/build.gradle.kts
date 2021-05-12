import java.util.stream.Collectors

plugins {
    `java-library`
}

dependencies {
    val kotlinVersion = property("kotlin.version")
    api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.springframework.boot:spring-boot-gradle-plugin")
    api(project(":plugin:generator-plugin"))
    api(project(":plugin:dist-plugin"))
    api(project(":plugin:profile-plugin"))
    api(project(":plugin:autodoc-plugin")) {
        exclude("ch.qos.logback", "logback-classic")
    }
    api("mysql:mysql-connector-java")
    api("com.oracle.database.jdbc:ojdbc8")
    api(fileTree(mapOf("dir" to "libs")))
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
                mapOf("tokens" to mapOf("summer.version" to project.version)),
                org.apache.tools.ant.filters.ReplaceTokens::class.java
            )
        }
    }
}