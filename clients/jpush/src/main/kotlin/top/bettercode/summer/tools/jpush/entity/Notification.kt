package top.bettercode.summer.tools.jpush.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Notification(

        @field:JsonProperty("android")
        val android: Android,

        @field:JsonProperty("ios")
        val ios: Ios
) {
    @JvmOverloads
    constructor(
            alert: String? = null,
            extras: Map<String, Any?>? = null,
            title: String? = null
    ) : this(android = Android(alert, title, extras), ios = Ios(alert, extras))
}