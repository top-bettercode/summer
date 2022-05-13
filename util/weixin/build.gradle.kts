plugins { `java-library` }
apply {
    plugin("org.springframework.boot")
    plugin("org.jetbrains.kotlin.plugin.spring")
}

dependencies {
    api(project(":framework:web"))

    testImplementation(project(":util:test"))
}

