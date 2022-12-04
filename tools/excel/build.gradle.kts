plugins {
    `java-library`
}

apply {
    plugin("summer.publish")
}

dependencies {
    api(project(":web"))

    api("org.javassist:javassist")
    api("org.dhatim:fastexcel")

    compileOnly("org.dhatim:fastexcel-reader")
    testImplementation("org.dhatim:fastexcel-reader")

    testImplementation(project(":test"))
}


