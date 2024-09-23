plugins {
    `java-library`
}

dependencies {
    api(project(":web"))

    testImplementation("com.hikvision.ga:artemis-http-client")
    testImplementation(project(":test"))
}