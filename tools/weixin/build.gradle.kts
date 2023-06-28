plugins {
    `java-library`
}

dependencies {
    api(project(":web"))

    compileOnly("org.bouncycastle:bcprov-jdk18on")

    testImplementation(project(":test"))
    testImplementation("org.bouncycastle:bcprov-jdk18on")
}

