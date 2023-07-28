package top.bettercode.summer.tools.mobile.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class RecordItem(

        @field:JsonProperty("resDesc")
        val resDesc: String? = null,

        @field:JsonProperty("resCode")
        val resCode: String? = null
)