package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Regeocode(

        @field:JsonProperty("addressComponent")
        val addressComponent: AddressComponent? = null,

        @field:JsonProperty("formatted_address")
        val formattedAddress: String? = null
)