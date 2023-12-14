plugins {
    `java-library`
}

dependencies {
    implementation(project(":tools:tools"))
    implementation(fileTree("libs"))

    compileOnly("com.google.ortools:ortools-java")
    testImplementation("com.google.ortools:ortools-java")

    testImplementation(project(":tools:excel"))
    testImplementation("org.dhatim:fastexcel-reader")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks {

//    test {
//        environment("LD_LIBRARY_PATH", file("build/native").absolutePath)
//    }

    "jar"(Jar::class) {
        from(fileTree(mapOf("dir" to "libs")).files.map { zipTree(it) })
    }
}

