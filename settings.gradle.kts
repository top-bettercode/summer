pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":env")
include(":web")
include(":data-jpa")
include(":security")
include(":test")

include(":summer-plugin")
include(":publish-plugin")

include(":tools:generator")
include(":tools:tools")
include(":tools:excel")
include(":tools:ueditor")
include(":tools:sap")
include(":tools:weixin")
include(":tools:sms")
include(":tools:mobile-query")
include(":tools:rapidauth")
include(":tools:weather")
include(":tools:jpush")
include(":tools:qvod")
include(":tools:amap")

include(":summer-bom")