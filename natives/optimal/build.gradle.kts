plugins {
    `java-library`
}

dependencies {
    api(project(":tools:tools"))
    api(fileTree("libs"))

    compileOnly("com.google.ortools:ortools-java")
    testImplementation("com.google.ortools:ortools-java")

    testImplementation("com.gurobi:gurobi:11.0.0")

    testImplementation(project(":tools:excel"))
    testImplementation("org.dhatim:fastexcel-reader")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks {

    "jar"(Jar::class) {
        from(fileTree(mapOf("dir" to "libs")).files.map { zipTree(it) })
    }
}

