plugins {
    kotlin("jvm").version("1.9.23")
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/gradle-plugin/")
    gradlePluginPortal()
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${KotlinVersion.CURRENT}")
    implementation("org.jetbrains.kotlin:kotlin-allopen:${KotlinVersion.CURRENT}")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.0.2")
    implementation("org.graalvm.buildtools:native-gradle-plugin:0.10.2")
    //--------------------------------------------
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
    implementation("com.gradle.publish:plugin-publish-plugin:1.2.1")
//    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
}
