package top.bettercode.summer.util.wechat.support.miniprogram.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.util.wechat.support.WeixinResponse

data class JsSession(
    @field:JsonProperty("openid")
    val openid: String? = null,

    @field:JsonProperty("session_key")
    val sessionKey: String? = null,

    @field:JsonProperty("unionid")
    val unionid: String? = null
) : WeixinResponse()