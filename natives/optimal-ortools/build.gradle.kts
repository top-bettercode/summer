plugins {
    `java-library`
}

dependencies {
    api(project(":natives:optimal"))
    api("com.google.ortools:ortools-java")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

