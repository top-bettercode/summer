dependencies {
    api(project(":framework:web"))
    api("com.auth0:java-jwt")
    api("com.tencentcloudapi:tencentcloud-sdk-java")

    compileOnly("com.qcloud:vod_api") {
        exclude(group = "org.slf4j")
    }
    testImplementation("com.qcloud:vod_api") {
        exclude(group = "org.slf4j")
    }
    testImplementation(project(":util:test"))
}
