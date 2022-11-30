plugins {
    `java-library`
}

dependencies {
    api(project(":util:autodoc-core"))
    api(project(":framework:web"))
    api("org.springframework.boot:spring-boot-starter-test")

    //util
    api(project(":util:api-sign"))
    api(project(":util:generator"))
    api("org.atteo:evo-inflector")

    compileOnly(project(":framework:data-jpa"))

    //test
    testImplementation(project(":framework:data-jpa"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testImplementation("xerces:xercesImpl")
}