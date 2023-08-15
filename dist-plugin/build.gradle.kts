plugins {
    `java-library`
}

dependencies {
    api(gradleApi())
    api("org.jetbrains.kotlin:kotlin-gradle-plugin")
    api("org.yaml:snakeyaml")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}