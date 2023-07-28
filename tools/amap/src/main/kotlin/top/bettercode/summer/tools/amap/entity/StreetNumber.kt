package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class StreetNumber(

        @field:JsonProperty("number")
        val number: String? = null,

        @field:JsonProperty("location")
        val location: String? = null,

        @field:JsonProperty("direction")
        val direction: String? = null,

        @field:JsonProperty("distance")
        val distance: String? = null,

        @field:JsonProperty("street")
        val street: String? = null
)