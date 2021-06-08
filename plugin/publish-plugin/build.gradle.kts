plugins { `java-library` }
dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.dokka:dokka-gradle-plugin")
    api("org.jetbrains.dokka:kotlin-as-java-plugin")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("com.gradle.publish:plugin-publish-plugin")
    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin")

    compileOnly("com.jfrog.bintray.gradle:gradle-bintray-plugin")
    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle") {
        exclude(module = "groovy-all")
    }
}
