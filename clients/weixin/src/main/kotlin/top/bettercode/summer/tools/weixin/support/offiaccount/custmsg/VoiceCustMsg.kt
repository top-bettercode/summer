package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

class VoiceCustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        override val touser: String,

        mediaId: String,

        @field:JsonProperty("msgtype")
        override val msgtype: String = "voice",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice) {

    @field:JsonProperty("voice")
    val voice: CustMedia = CustMedia(mediaId)
}


