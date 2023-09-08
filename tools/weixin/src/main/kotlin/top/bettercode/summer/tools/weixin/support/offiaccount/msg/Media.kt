package top.bettercode.summer.tools.weixin.support.offiaccount.msg

import com.fasterxml.jackson.annotation.JsonProperty

data class Media @JvmOverloads constructor(

        @field:JsonProperty("MediaId")
        val mediaId: String,

        @field:JsonProperty("Title")
        val title: String? = null,

        @field:JsonProperty("Description")
        val description: String? = null,

        )
