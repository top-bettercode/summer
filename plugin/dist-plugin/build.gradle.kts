plugins { `java-library` }

dependencies {
    api("gradle.plugin.com.github.alexeylisyutenko:windows-service-plugin")
    api(project(":plugin:profile-plugin"))
}
