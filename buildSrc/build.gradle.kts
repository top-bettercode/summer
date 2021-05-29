import java.util.Properties

configurations {
    all {
        resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
    }
}

repositories {
    mavenLocal()
//    maven("https://maven.aliyun.com/repository/gradle-plugin")
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
    gradlePluginPortal()
    maven("https://oss.jfrog.org/oss-snapshot-local")
}

dependencies {
    val properties = Properties()
    properties.load(project.file("../gradle.properties").inputStream())
    val kotlinVersion = properties.getProperty("kotlin.version")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")

    implementation("io.spring.gradle:dependency-management-plugin:1.0.11.RELEASE")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.24.3")

    implementation("cn.bestwu.summer:publish-plugin:0.0.9-SNAPSHOT")
}