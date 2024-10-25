package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Geocode(

        @field:JsonProperty("formatted_address")
        val formattedAddress: String? = null,

        @field:JsonProperty("country")
        val country: String? = null,

        @field:JsonProperty("province")
        val province: String? = null,

        @field:JsonProperty("citycode")
        val citycode: String? = null,

        @field:JsonProperty("city")
        val city: String? = null,

        @field:JsonProperty("district")
        val district: String? = null,

        @field:JsonProperty("township")
        val township: String? = null,

        @field:JsonProperty("neighborhood")
        val neighborhood: Neighborhood? = null,

        @field:JsonProperty("building")
        val building: Building? = null,

        @field:JsonProperty("adcode")
        val adcode: String? = null,

        @field:JsonProperty("street")
        val street: String? = null,

        @field:JsonProperty("number")
        val number: String? = null,

        @field:JsonProperty("location")
        val location: String? = null,

        @field:JsonProperty("level")
        val level: String? = null
)