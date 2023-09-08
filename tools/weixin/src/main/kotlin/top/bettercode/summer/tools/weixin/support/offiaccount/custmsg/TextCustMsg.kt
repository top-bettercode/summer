package top.bettercode.summer.tools.weixin.support.offiaccount.custmsg

import com.fasterxml.jackson.annotation.JsonProperty

class TextCustMsg @JvmOverloads constructor(

        @field:JsonProperty("touser")
        override val touser: String,

        content: String,

        @field:JsonProperty("msgtype")
        override val msgtype: String = "text",

        @field:JsonProperty("customservice")
        override val customservice: CustomService? = null,
) : CustMsg(touser, msgtype, customservice) {

    @field:JsonProperty("text")
    val text: Text = Text(content)
}

data class Text(

        @field:JsonProperty("content")
        val content: String
)
