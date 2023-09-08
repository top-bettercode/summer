package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

data class MusicCustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        override val touser: String,

        @field:JsonProperty("music")
        val music: Music,

        @field:JsonProperty("msgtype")
        override val msgtype: String = "music",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice)

data class Music @JvmOverloads constructor(

        @field:JsonProperty("thumb_media_id")
        val thumbMediaId: String,

        @field:JsonProperty("musicurl")
        val musicurl: String,

        @field:JsonProperty("hqmusicurl")
        val hqmusicurl: String,

        @field:JsonProperty("title")
        val title: String = "",

        @field:JsonProperty("description")
        val description: String = "",

        )
