plugins {
    `java-library`
}

dependencies {
    api(project(":natives:optimal"))

    compileOnly(project(":tools:excel"))

    testImplementation(project(":natives:optimal-copt"))
    testImplementation(project(":natives:optimal-cplex"))
    testImplementation(project(":natives:optimal-gurobi"))
    testImplementation(project(":natives:optimal-ortools"))

    testImplementation(project(":tools:excel"))
    testImplementation("org.dhatim:fastexcel-reader")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks{
    test {
        file("build/native").mkdirs()
        environment("LD_LIBRARY_PATH", file("build/native").absolutePath)
    }
}