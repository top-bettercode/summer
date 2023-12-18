plugins {
    `java-library`
}

dependencies {
    api(project(":web"))

    testImplementation(project(":test"))
}
