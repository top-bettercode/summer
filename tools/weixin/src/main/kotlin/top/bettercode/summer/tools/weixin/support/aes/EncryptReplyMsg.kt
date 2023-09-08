package top.bettercode.summer.tools.weixin.support.aes

import com.fasterxml.jackson.annotation.JsonProperty

data class EncryptReplyMsg(

        @field:JsonProperty("Encrypt")
        val encrypt: String? = null,

        @field:JsonProperty("MsgSignature")
        val msgSignature: String? = null,

        @field:JsonProperty("TimeStamp")
        val timeStamp: Long? = null,

        @field:JsonProperty("Nonce")
        val nonce: String? = null
)