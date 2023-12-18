package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsapiTicket @JvmOverloads constructor(

        @field:JsonProperty("ticket")
        var ticket: String? = null,

        @field:JsonProperty("expires_in")
        var expiresIn: Int? = null
) : WeixinResponse()