package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class JsapiSignature @JvmOverloads constructor(

        @field:JsonProperty("signature")
        var signature: String? = null,

        @field:JsonProperty("appid")
        var appid: String? = null,

        @field:JsonProperty("nonceStr")
        var nonceStr: String? = null,

        @field:JsonProperty("timestamp")
        var timestamp: String? = null
)