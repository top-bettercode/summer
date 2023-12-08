plugins {
    `java-library`
}

version = "6.3.13_20181119"

dependencies {
    implementation(project(":tools:tools"))
    implementation(fileTree("libs"))

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

tasks {

    "jar"(Jar::class) {
        from(fileTree(mapOf("dir" to "libs")).files.map { zipTree(it) })
    }
}

