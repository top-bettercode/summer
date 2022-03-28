plugins {
    `java-library`
}

dependencies {
    api(project(":framework:config"))
    api(project(":util:autodoc-gen"))
    api("org.springframework.boot:spring-boot-starter-test")
    api("xerces:xercesImpl")

    compileOnly(project(":framework:security"))

    testImplementation(project(":framework:security"))
}

