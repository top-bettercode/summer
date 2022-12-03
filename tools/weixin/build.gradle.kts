plugins {
    `java-library`
}

apply {
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.jetbrains.kotlin.plugin.spring")
    plugin("summer.kotlin-publish")
}

dependencies {
    api(project(":web"))

    compileOnly("org.bouncycastle:bcprov-jdk18on")

    testImplementation(project(":test"))
}

