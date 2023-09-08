package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

data class MiniProgramPageCustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        override val touser: String,

        @field:JsonProperty("miniprogrampage")
        val miniprogrampage: Miniprogrampage,

        @field:JsonProperty("msgtype")
        override val msgtype: String = "miniprogrampage",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice)

data class Miniprogrampage(

        @field:JsonProperty("appid")
        val appid: String,

        @field:JsonProperty("title")
        val title: String,

        @field:JsonProperty("thumb_media_id")
        val thumbMediaId: String,

        @field:JsonProperty("pagepath")
        val pagepath: String,
)
