
dependencies {
    compile(kotlin("reflect"))
    compile("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    compile("com.gradle.publish:plugin-publish-plugin:0.10.0")
    compile("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle:4.8.1") {
        exclude(module = "groovy-all")
    }
    testCompile(kotlin("test-junit"))
}
