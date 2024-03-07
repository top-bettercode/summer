plugins {
    `java-library`
}

dependencies {
    api(project(":natives:optimal"))
    api("com.google.ortools:ortools-java")

    testImplementation("org.slf4j:slf4j-simple")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

