repositories {
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    compile("com.querydsl:querydsl-apt:4.2.1:jpa")

    compile("cn.bestwu.gradle:publish-plugin:0.0.31")
    compile("org.jfrog.buildinfo:build-info-extractor-gradle:4.8.1")
}