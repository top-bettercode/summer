plugins {
    `java-library`
}

dependencies {
    api(project(":natives:optimal"))

    compileOnly("com.google.ortools:ortools-java")
    compileOnly(project(":tools:excel"))

    testImplementation("com.google.ortools:ortools-java")
    testImplementation("com.gurobi:gurobi:11.0.0")

    testImplementation(project(":tools:excel"))
    testImplementation("org.dhatim:fastexcel-reader")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

