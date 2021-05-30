plugins {
    `java-library`
}

subprojects {
    if (arrayOf("excel", "wechat", "ueditor").contains(name)) {
        apply {
            plugin("summer.publish")
        }
    } else {
        apply {
            plugin("org.jetbrains.kotlin.jvm")
            plugin("org.jetbrains.kotlin.plugin.spring")
            plugin("summer.kotlin-publish")
        }
    }
}
