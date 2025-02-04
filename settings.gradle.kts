pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        gradlePluginPortal()
        maven("https://maven.aliyun.com/repository/public/")
        mavenCentral()
    }
}

include(":summer-bom")
include(":summer-cloud-bom")

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
include(":tools:configuration-processor")
include(":tools:optimal")
include(":tools:optimal-gurobi")
include(":tools:optimal-ortools")
include(":tools:recipe")
include(":tools:ktrader-broker-ctp")

include(":clients:weixin")
include(":clients:sms")
include(":clients:mobile-query")
include(":clients:rapidauth")
include(":clients:weather")
include(":clients:jpush")
include(":clients:qvod")
include(":clients:amap")
include(":clients:pay")
include(":clients:hikvision")
include(":clients:feishu")

include(":natives:sap")
include(":natives:optimal-copt")
include(":natives:optimal-cplex")
include(":natives:ctpapi")


