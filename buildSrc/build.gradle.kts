import java.util.Properties

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
    val kotlinVersion = properties.getProperty("kotlin.version")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")

    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.5.0")

    implementation("top.bettercode.summer:publish-plugin:0.0.10-SNAPSHOT")
}