plugins {
    `java-library`
}

dependencies {
    api(project(":web"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    api("org.apache.httpcomponents:httpclient")


    testImplementation(project(":test"))
}

