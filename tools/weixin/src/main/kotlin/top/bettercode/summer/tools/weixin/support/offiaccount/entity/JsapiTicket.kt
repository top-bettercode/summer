package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsapiTicket(

    @field:JsonProperty("ticket")
    val ticket: String? = null,

    @field:JsonProperty("expires_in")
    val expiresIn: Int? = null
) : WeixinResponse()