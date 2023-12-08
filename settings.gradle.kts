pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        gradlePluginPortal()
        maven("https://maven.aliyun.com/repository/public/")
        mavenCentral()
    }
}

include(":env")
include(":web")
include(":data-jpa")
include(":security")
include(":test")

include(":dist-plugin")
include(":summer-plugin")
include(":publish-plugin")

include(":tools:generator")
include(":tools:tools")
include(":tools:excel")
include(":tools:ueditor")
include(":tools:weixin")
include(":tools:sms")
include(":tools:mobile-query")
include(":tools:rapidauth")
include(":tools:weather")
include(":tools:jpush")
include(":tools:qvod")
include(":tools:amap")
include(":tools:sap")
include(":tools:configuration-processor")
include(":tools:pay")
include(":tools:optimal")
include(":tools:ctpapi")


include(":summer-bom")
include(":summer-cloud-bom")
