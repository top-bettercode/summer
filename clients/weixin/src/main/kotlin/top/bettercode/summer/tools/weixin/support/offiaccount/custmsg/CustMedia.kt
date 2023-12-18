package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

data class CustMedia(

        @field:JsonProperty("media_id")
        val mediaId: String
)
