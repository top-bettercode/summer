plugins { `java-library` }

dependencies {
    api(project(":util:common-lang"))
    api("org.springframework.boot:spring-boot-starter-web")


    testImplementation(project(":util:test"))
}


