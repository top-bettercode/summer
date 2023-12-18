package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

data class VideoCustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        override val touser: String,

        @field:JsonProperty("video")
        val video: Video,

        @field:JsonProperty("msgtype")
        override val msgtype: String = "video",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice)

data class Video @JvmOverloads constructor(

        @field:JsonProperty("media_id")
        val mediaId: String,

        @field:JsonProperty("thumb_media_id")
        val thumbMediaId: String,

        @field:JsonProperty("title")
        val title: String = "",

        @field:JsonProperty("description")
        val description: String = "",

        )
