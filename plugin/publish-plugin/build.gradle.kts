plugins { `java-library` }
dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("com.jfrog.bintray.gradle:gradle-bintray-plugin")
    api("com.gradle.publish:plugin-publish-plugin")
    api("org.jetbrains.dokka:dokka-gradle-plugin")
    api("org.jetbrains.dokka:kotlin-as-java-plugin")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle") {
        exclude(module = "groovy-all")
    }
}
