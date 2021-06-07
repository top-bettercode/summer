plugins { `java-library` }

dependencies {
    api(project(":framework:web"))
    api("org.springframework:spring-tx")

    compileOnly("top.bettercode.wechat:weixin-mp")
    compileOnly("top.bettercode.wechat:weixin-app")

    testImplementation(project(":util:test"))
}

