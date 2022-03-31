plugins { `java-library` }
dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.dokka:kotlin-as-java-plugin")
    api("org.jetbrains.dokka:dokka-gradle-plugin")
    api("com.gradle.publish:plugin-publish-plugin")
//    api("io.codearte.gradle.nexus:gradle-nexus-staging-plugin")
}
