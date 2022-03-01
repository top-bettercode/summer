package top.bettercode.summer.util.wechat.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.util.wechat.support.Response

data class JsapiTicket(

    @field:JsonProperty("ticket")
    val ticket: String? = null,

    @field:JsonProperty("expires_in")
    val expiresIn: Int? = null
) : Response()