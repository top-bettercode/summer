plugins {
    kotlin("jvm").version("1.6.21")
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
    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.6.13")
    //--------------------------------------------
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
    implementation("com.gradle.publish:plugin-publish-plugin:1.1.0")
//    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
}
