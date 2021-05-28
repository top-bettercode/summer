plugins {
    `java-library`
}

dependencies {
    api(project(":framework:web"))
    api("org.springframework.boot:spring-boot-starter-test")
    api(project(":util:autodoc-gen"))
    api("org.springframework.hateoas:spring-hateoas")

    compileOnly(project(":framework:security-resource"))
}

