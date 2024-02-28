plugins {
    `java-library`
}

dependencies {
    api(project(":tools:tools"))

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}
