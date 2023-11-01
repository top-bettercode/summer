plugins {
    `java-library`
}

dependencies {
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.slf4j:slf4j-api")

    compileOnly("ch.qos.logback:logback-classic")
    compileOnly("org.springframework:spring-webmvc")
    testImplementation("org.springframework:spring-webmvc")
    compileOnly("javax.servlet:javax.servlet-api")

    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    compileOnly("com.squareup.okhttp3:okhttp")
    testImplementation("com.squareup.okhttp3:okhttp")

    compileOnly("org.jsoup:jsoup")
    testImplementation("org.jsoup:jsoup")


    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

