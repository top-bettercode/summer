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

    implementation("cn.bestwu.summer:publish-plugin:0.0.1-SNAPSHOT")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.15.1")
}