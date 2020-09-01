import org.apache.tools.ant.filters.ReplaceTokens
plugins { `java-library` }

dependencies {
    api(project(":util:autodoc-core"))
    api(project(":plugin:profile-plugin"))
}

tasks {
    "processResources"(ProcessResources::class) {
        outputs.upToDateWhen { false }
        filesMatching(setOf("**/*.properties")) {
            filter(mapOf("tokens" to mapOf("autodoc.version.default" to project.version)), ReplaceTokens::class.java)
        }
    }
}