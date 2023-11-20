package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.weixin.support.WeixinResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class SnsapiUserinfo @JvmOverloads constructor(

        @field:JsonProperty("openid")
        var openid: String? = null,

        @field:JsonProperty("nickname")
        var nickname: String? = null,

        @field:JsonProperty("sex")
        var sex: Int? = null,

        @field:JsonProperty("province")
        var province: String? = null,

        @field:JsonProperty("city")
        var city: String? = null,

        @field:JsonProperty("country")
        var country: String? = null,

        @field:JsonProperty("headimgurl")
        var headimgurl: String? = null,

        @field:JsonProperty("privilege")
        var privilege: List<String?>? = null,

        @field:JsonProperty("unionid")
        var unionid: String? = null
) : WeixinResponse()