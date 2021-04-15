import java.util.Properties

configurations {
    all {
        resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
    }
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/gradle-plugin")
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

    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.2.5.RELEASE")

    implementation("com.querydsl:querydsl-apt:4.3.0:jpa")

    implementation("cn.bestwu.summer:publish-plugin:0.0.7-SNAPSHOT")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.15.1")
}