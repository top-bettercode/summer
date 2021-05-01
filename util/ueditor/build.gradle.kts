plugins { `java-library` }
dependencies {
    api("commons-codec:commons-codec:1.10")
    api("org.json:json:20180813")
    api("org.springframework:spring-core")
    api(project(":util:common-lang"))
    compileOnly("javax.servlet:javax.servlet-api")
}
