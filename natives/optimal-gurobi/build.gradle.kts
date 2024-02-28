plugins {
    `java-library`
}

dependencies {
    api(project(":natives:optimal"))
    api("com.gurobi:gurobi")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

