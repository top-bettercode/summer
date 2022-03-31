plugins { `java-library` }

dependencies {
    api("top.bettercode.summer:windows-service-plugin")
    compileOnly(project(":plugin:profile-plugin"))
}
