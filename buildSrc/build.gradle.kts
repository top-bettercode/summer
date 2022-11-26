plugins {
    kotlin("jvm").version("1.6.10")
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
    val springBootVersion = property("spring-boot.version")
    val kotlinVersion = property("kotlin.version")
    val kotlinxCoroutinesVersion = property("kotlinx-coroutines.version")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")

    //--------------------------------------------
    //publish plugin dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutinesVersion}")
    implementation("org.jetbrains.dokka:kotlin-as-java-plugin:$kotlinVersion")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
    implementation("com.gradle.publish:plugin-publish-plugin:1.1.0")
//    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")

//    implementation("top.bettercode.summer:publish-plugin:0.0.18-SNAPSHOT")
}

tasks {
    "processResources"(ProcessResources::class) {
        outputs.upToDateWhen { false }
        filesMatching(setOf("**/*.properties")) {
            filter(
                mapOf("tokens" to project.properties.filter { it.key.endsWith("version") }),
                org.apache.tools.ant.filters.ReplaceTokens::class.java
            )
        }
    }
}
