plugins {
    `java-library`
}

dependencies {
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.springframework.boot:spring-boot-starter-web")

    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    compileOnly("com.squareup.okhttp3:okhttp")
    testImplementation("com.squareup.okhttp3:okhttp")
    compileOnly("org.jsoup:jsoup")
    testImplementation("org.jsoup:jsoup")


    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

