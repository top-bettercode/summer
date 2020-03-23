configurations {
    filter { arrayOf("compile", "testCompile").contains(it.name) }.forEach { it.exclude("org.codehaus.jackson") }
    all {
        resolutionStrategy.cacheChangingModulesFor(1, TimeUnit.SECONDS)
    }
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    jcenter()
    gradlePluginPortal()
    maven("http://oss.jfrog.org/oss-snapshot-local")
}

val kotlin_version = "1.3.10"

dependencies {
    compile("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    compile("org.jetbrains.kotlin:kotlin-allopen:$kotlin_version")

    compile("org.springframework.boot:spring-boot-gradle-plugin:2.1.8.RELEASE")

    compile("com.querydsl:querydsl-apt:4.2.1:jpa")

    compile("cn.bestwu.gradle:publish-plugin:0.0.32-SNAPSHOT")
    compile("org.jfrog.buildinfo:build-info-extractor-gradle:4.8.1")
}