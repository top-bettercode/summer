
dependencies {
    compile(kotlin("reflect"))
    compile("com.jfrog.bintray.gradle:gradle-bintray-plugin")
    compile("com.gradle.publish:plugin-publish-plugin")
    compile("org.jetbrains.dokka:dokka-gradle-plugin")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle") {
        exclude(module = "groovy-all")
    }
    testCompile(kotlin("test-junit"))
}
