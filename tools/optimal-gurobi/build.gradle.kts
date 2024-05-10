plugins {
    `java-library`
}

dependencies {
    api(project(":tools:optimal"))
    api("com.gurobi:gurobi")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

