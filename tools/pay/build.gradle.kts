plugins {
    `java-library`
}

dependencies {
    api(project(":web"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    api("org.apache.httpcomponents:httpclient:4.5.14")


    testImplementation(project(":test"))
}

