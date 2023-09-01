plugins {
    `java-library`
}

dependencies {
    api(project(":web"))
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    api("com.github.wechatpay-apiv3:wechatpay-java")
    api("com.squareup.okhttp3:okhttp")

    testImplementation(project(":test"))
}

