plugins {
    `java-library`
}

dependencies {
    api(project(":web"))
    api("com.github.easonjim:com.sap.conn.jco.sapjco3")

    compileOnly(project(":tools:generator"))
    testImplementation(project(":tools:generator"))

    testImplementation(project(":test"))
}
