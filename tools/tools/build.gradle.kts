plugins {
    `java-library`
}

dependencies {
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.springframework.boot:spring-boot-starter-web")

    compileOnly("org.jsoup:jsoup")
    testImplementation("org.jsoup:jsoup")


    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

