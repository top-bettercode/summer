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

val kotlin_version = "1.3.70"

dependencies {
    compile("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    compile("org.jetbrains.kotlin:kotlin-allopen:$kotlin_version")

    compile("org.springframework.boot:spring-boot-gradle-plugin:2.2.5.RELEASE")

    compile("com.querydsl:querydsl-apt:4.3.0:jpa")

    compile("cn.bestwu.summer:publish-plugin:0.0.1-SNAPSHOT")
    compile("org.jfrog.buildinfo:build-info-extractor-gradle:4.15.1")
}