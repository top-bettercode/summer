package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class StreetNumber(

        @field:JsonProperty("number")
        val number: Any? = null,

        @field:JsonProperty("location")
        val location: Any? = null,

        @field:JsonProperty("direction")
        val direction: Any? = null,

        @field:JsonProperty("distance")
        val distance: Any? = null,

        @field:JsonProperty("street")
        val street: Any? = null
)