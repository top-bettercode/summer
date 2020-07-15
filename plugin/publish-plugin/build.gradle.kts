plugins { `java-library` }
dependencies {
    api(kotlin("reflect"))
    api("com.jfrog.bintray.gradle:gradle-bintray-plugin")
    api("com.gradle.publish:plugin-publish-plugin")
    api("org.jetbrains.dokka:dokka-gradle-plugin")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle") {
        exclude(module = "groovy-all")
    }
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
