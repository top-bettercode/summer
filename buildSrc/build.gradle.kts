plugins {
    kotlin("jvm").version("1.3.70")
}

configurations {
    all {
        resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
    }
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/gradle-plugin")
    maven("https://maven.aliyun.com/repository/public")
    jcenter()
    gradlePluginPortal()
    maven("https://oss.jfrog.org/oss-snapshot-local")
}

val kotlin_version = "1.3.70"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlin_version")

    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.2.5.RELEASE")

    implementation("com.querydsl:querydsl-apt:4.3.0:jpa")

//    implementation("cn.bestwu.summer:publish-plugin:0.0.2-SNAPSHOT")
    implementation(kotlin("reflect"))
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    implementation("com.gradle.publish:plugin-publish-plugin:0.10.0")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:0.10.1")

    compileOnly("org.jfrog.buildinfo:build-info-extractor-gradle:4.15.1") {
        exclude(module = "groovy-all")
    }

    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.15.1")
}