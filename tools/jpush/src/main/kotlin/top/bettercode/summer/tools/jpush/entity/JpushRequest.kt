package top.bettercode.summer.tools.jpush.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class JpushRequest @JvmOverloads constructor(

    @field:JsonProperty("audience")
    val audience: Audience,

    @field:JsonProperty("notification")
    val notification: Notification,

    @field:JsonProperty("message")
    val message: Message? = null,

    @field:JsonProperty("options")
    var options: Options? = null,

    @field:JsonProperty("cid")
    var cid: String? = null,

    @field:JsonProperty("platform")
    val platform: String = "all"
)