plugins {
    `java-library`
}

dependencies {
    api(project(":web"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    compileOnly("org.bouncycastle:bcprov-jdk18on")

    testImplementation(project(":test"))
    testImplementation("org.bouncycastle:bcprov-jdk18on")
}

