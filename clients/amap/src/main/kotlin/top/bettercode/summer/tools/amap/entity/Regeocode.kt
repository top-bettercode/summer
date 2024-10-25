package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Regeocode(
    @field:JsonProperty("formatted_address")
    val formattedAddress: String? = null,

    @field:JsonProperty("addressComponent")
    val addressComponent: AddressComponent? = null,

    @field:JsonProperty("roadinters")
    val roadinters: List<Roadinter>? = null,

    @field:JsonProperty("aois")
    val aois: List<Aoi>? = null,

    @field:JsonProperty("roads")
    val roads: List<Road>? = null,

    @field:JsonProperty("pois")
    val pois: List<Poi>? = null

)