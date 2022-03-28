import java.util.*

plugins {
    `embedded-kotlin`
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/gradle-plugin/")
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://s01.oss.sonatype.org/content/groups/public/")
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    val properties = Properties()
    properties.load(project.file("../gradle.properties").inputStream())
    val springVersion = properties.getProperty("spring.version")
    val kotlinVersion = properties.getProperty("kotlin.version")
    val kotlinCoroutinesVersion = properties.getProperty("kotlin-coroutines.version")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")

    implementation("org.springframework.boot:spring-boot-gradle-plugin:${springVersion}")

    //--------------------------------------------
    //publish plugin dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinCoroutinesVersion}")
    implementation("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.10")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
    implementation("com.gradle.publish:plugin-publish-plugin:0.20.0")
//    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0") {
//        exclude("org.jetbrains.kotlin")
//    }
}