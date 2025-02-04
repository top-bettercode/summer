plugins {
    `java-library`
}

dependencies {
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    api("org.slf4j:slf4j-api")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    compileOnly("ch.qos.logback:logback-classic")
    compileOnly("org.springframework:spring-webmvc")
    testImplementation("org.springframework:spring-webmvc")
    compileOnly("javax.servlet:javax.servlet-api")

    compileOnly("com.github.ben-manes.caffeine:caffeine")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")

    compileOnly("com.squareup.okhttp3:okhttp")
    testImplementation("com.squareup.okhttp3:okhttp")

    compileOnly("org.jsoup:jsoup")
    testImplementation("org.jsoup:jsoup")


    testImplementation("org.springframework.boot:spring-boot-starter-logging")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

