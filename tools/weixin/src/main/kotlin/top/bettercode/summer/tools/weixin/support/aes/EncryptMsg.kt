package top.bettercode.summer.tools.weixin.support.aes

import com.fasterxml.jackson.annotation.JsonProperty

data class EncryptMsg(

        @field:JsonProperty("ToUserName")
        val toUserName: String? = null,

        @field:JsonProperty("Encrypt")
        val encrypt: String? = null
)