plugins {
    `java-library`
}

dependencies {
    api(project(":web"))
    api("com.larksuite.oapi:oapi-sdk")

    testImplementation(project(":test"))
}
