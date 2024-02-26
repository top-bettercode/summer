plugins {
    `java-library`
}

dependencies {
    api(project(":natives:optimal"))
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    compileOnly("com.google.ortools:ortools-java")
    compileOnly(project(":tools:excel"))

    testImplementation("com.google.ortools:ortools-java")
    testImplementation("com.gurobi:gurobi")

    testImplementation(project(":tools:excel"))
    testImplementation("org.dhatim:fastexcel-reader")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

