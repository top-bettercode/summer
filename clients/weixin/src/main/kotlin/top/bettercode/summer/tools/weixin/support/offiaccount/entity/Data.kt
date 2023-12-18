package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty


data class Data @JvmOverloads constructor(

        @field:JsonProperty("value")
        var value: String,

        @field:JsonProperty("color")
        var color: String? = null
)