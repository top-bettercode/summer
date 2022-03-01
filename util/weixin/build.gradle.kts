plugins { `java-library` }

dependencies {
    api(project(":framework:web"))

    testImplementation(project(":util:test"))
}

