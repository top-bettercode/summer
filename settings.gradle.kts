pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        gradlePluginPortal()
    }
}

include(":framework:web")
include(":framework:data-jpa")
include(":framework:data-mybatis")
include(":framework:security-core")
include(":framework:security-server")
include(":framework:security-resource")
include(":framework:starter-logging")

include(":util:common-lang")
include(":util:api-sign")
include(":util:excel")
include(":util:autodoc-core")
include(":util:autodoc-gen")
include(":util:generator")

include(":plugin:autodoc-plugin")
include(":plugin:dist-plugin")
include(":plugin:generator-plugin")
include(":plugin:profile-plugin")
include(":plugin:publish-plugin")
