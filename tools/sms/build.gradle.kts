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

    testImplementation(project(":test"))
}