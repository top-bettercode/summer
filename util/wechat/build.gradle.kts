plugins { `java-library` }

dependencies {
    api(project(":framework:web"))

    compileOnly("top.bettercode.wechat:weixin-mp")
    compileOnly("top.bettercode.wechat:weixin-app")

    testImplementation(project(":util:test"))
}

