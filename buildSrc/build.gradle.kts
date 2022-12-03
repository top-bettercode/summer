plugins {
    kotlin("jvm").version("1.6.21")
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${KotlinVersion.CURRENT}")
    implementation("org.jetbrains.kotlin:kotlin-allopen:${KotlinVersion.CURRENT}")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:${property("spring-boot.version")}")
    //--------------------------------------------
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${property("dokka.version")}")
    implementation("com.gradle.publish:plugin-publish-plugin:1.1.0")
//    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
}

tasks {
    @Suppress("UnstableApiUsage")
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
