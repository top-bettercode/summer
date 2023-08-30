plugins {
    `java-library`
}

dependencies {
    api(project(":web"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")


    testImplementation(project(":test"))
}

