plugins { `java-library` }

dependencies {
    api(project(":framework:web"))
    api("org.springframework:spring-tx")

    compileOnly("cn.bestwu.wechat:weixin-mp")
    compileOnly("cn.bestwu.wechat:weixin-app")

    testImplementation(project(":util:test"))
}

