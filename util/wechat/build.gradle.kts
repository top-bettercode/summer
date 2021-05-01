plugins { `java-library` }
dependencies {
    api(project(":framework:web"))
    api("org.springframework:spring-tx")

    api("cn.bestwu.wechat:weixin-mp")

    testImplementation(project(":util:test"))
}

