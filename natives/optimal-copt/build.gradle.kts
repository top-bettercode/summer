plugins {
    `java-library`
}

dependencies {
    api(project(":natives:optimal"))
    api(fileTree("libs"))

    testImplementation("org.slf4j:slf4j-simple")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks {
    test {
        file("build/native").mkdirs()
        environment("LD_LIBRARY_PATH", file("build/native").absolutePath)
    }
    "jar"(Jar::class) {
        from(fileTree(mapOf("dir" to "libs")).files.map {
            zipTree(it).matching {
                exclude("copt/Envr.class", "copt/EnvrConfig.class")
            }
        })
    }
}

