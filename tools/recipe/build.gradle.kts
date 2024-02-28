plugins {
    `java-library`
}

dependencies {
    api(project(":natives:optimal"))

    compileOnly(project(":tools:excel"))

    api(project(":natives:optimal-copt"))
    api(project(":natives:optimal-cplex"))
    api(project(":natives:optimal-gurobi"))
    api(project(":natives:optimal-ortools"))
    testImplementation(project(":tools:excel"))
    testImplementation("org.dhatim:fastexcel-reader")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

