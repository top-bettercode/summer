dependencies {
    api(project(":framework:web"))
    api("com.auth0:java-jwt")
    api("com.qcloud:vod_api") {
        exclude(group = "org.slf4j")
    }
    api("com.tencentcloudapi:tencentcloud-sdk-java:3.1.564")

    testImplementation(project(":util:test"))
}
