package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

class ImageCustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        override val touser: String,

        mediaId: String,

        @field:JsonProperty("msgtype")
        override val msgtype: String = "image",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice) {

    @field:JsonProperty("image")
    val image: CustMedia = CustMedia(mediaId)

}
