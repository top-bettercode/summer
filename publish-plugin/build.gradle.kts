plugins {
    `java-library`
}

dependencies {
    api(gradleApi())

    api("org.jetbrains.dokka:dokka-gradle-plugin")
    api("com.gradle.publish:plugin-publish-plugin")
//    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}
