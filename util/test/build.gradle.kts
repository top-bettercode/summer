plugins {
    `java-library`
}

dependencies {
    api(project(":framework:web"))
    api("org.springframework.boot:spring-boot-starter-test")
    api(project(":util:autodoc-gen"))

    compileOnly(project(":framework:security-resource"))
}

