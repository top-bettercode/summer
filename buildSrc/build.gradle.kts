plugins {
    kotlin("jvm").version("1.3.72")
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
    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.3.2.RELEASE")
    //--------------------------------------------
    implementation("com.gradle.publish:plugin-publish-plugin:1.1.0")
//    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
}
