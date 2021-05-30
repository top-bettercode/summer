plugins { `java-library` }

dependencies {
    api("commons-codec:commons-codec")
    api("org.json:json")
    api("org.springframework:spring-core")
    api(project(":util:common-lang"))
    compileOnly("javax.servlet:javax.servlet-api")
}
