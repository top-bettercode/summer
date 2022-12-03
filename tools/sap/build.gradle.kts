plugins {
    `java-library`
}

apply {
    plugin("summer.publish")
}

dependencies {
    api(project(":web"))
    api("com.github.easonjim:com.sap.conn.jco.sapjco3:3.0.11")

    compileOnly(project(":tools:generator"))
    testImplementation(project(":tools:generator"))

    testImplementation(project(":test"))
}
