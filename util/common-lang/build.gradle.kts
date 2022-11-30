plugins {
    `java-library`
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib")
    api("com.fasterxml.jackson.core:jackson-databind")

    compileOnly("org.jsoup:jsoup")
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testImplementation("org.jsoup:jsoup")
    testImplementation("org.slf4j:slf4j-simple")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

