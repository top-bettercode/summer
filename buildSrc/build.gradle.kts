import java.util.*

configurations {
    all {
        resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
    }
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
    val summerVersion = properties.getProperty("version")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")

    implementation("org.springframework.boot:spring-boot-gradle-plugin:${springVersion}")

    implementation("top.bettercode.summer:publish-plugin:0.0.13-SNAPSHOT")
//    implementation("top.bettercode.summer:publish-plugin:$summerVersion")
}