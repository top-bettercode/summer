package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class AddressComponent(

        @field:JsonProperty("city")
        val city: String? = null,

        @field:JsonProperty("province")
        val province: String? = null,

        @field:JsonProperty("adcode")
        val adcode: String? = null,

        @field:JsonProperty("district")
        val district: String? = null,

        @field:JsonProperty("towncode")
        val towncode: String? = null,

        @field:JsonProperty("country")
        val country: String? = null,

        @field:JsonProperty("township")
        val township: String? = null,

        @field:JsonProperty("citycode")
        val citycode: String? = null
)