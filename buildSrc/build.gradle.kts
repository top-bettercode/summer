import java.util.Properties

configurations {
    all {
        resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
    }
}

repositories {
    mavenLocal()
//    maven("https://maven.aliyun.com/repository/gradle-plugin")
    gradlePluginPortal()
    maven {
        credentials {
            username = "60b89f54395ee4198d8c67cf"
            password = "WnLfTlxXBq(k"
        }
        setUrl("https://packages.aliyun.com/maven/repository/2021488-snapshot-4ZYq5w/")
    }
    mavenCentral()
}

dependencies {
    val properties = Properties()
    properties.load(project.file("../gradle.properties").inputStream())
    val kotlinVersion = properties.getProperty("kotlin.version")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")

    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.5.0")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.24.3")

    implementation("cn.bestwu.summer:publish-plugin:0.0.9-SNAPSHOT")
}