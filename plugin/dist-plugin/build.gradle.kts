plugins { `java-library` }

dependencies {
    api("com.github.alexeylisyutenko:windows-service-plugin")
    compileOnly(project(":plugin:profile-plugin"))
}
