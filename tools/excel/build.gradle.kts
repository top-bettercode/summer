plugins {
    `java-library`
}

dependencies {
    api(project(":web"))

    api("org.javassist:javassist")
    api("org.dhatim:fastexcel")

    compileOnly("org.dhatim:fastexcel-reader")
    testImplementation("org.dhatim:fastexcel-reader")
    compileOnly("org.apache.poi:poi-ooxml")
    testImplementation("org.apache.poi:poi-ooxml")

    testImplementation(project(":test"))
}


