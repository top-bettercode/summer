package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Aoi(

    @field:JsonProperty("area")
    val area: String? = null,

    @field:JsonProperty("distance")
    val distance: String? = null,

    @field:JsonProperty("adcode")
    val adcode: String? = null,

    @field:JsonProperty("name")
    val name: String? = null,

    @field:JsonProperty("location")
    val location: String? = null,

    @field:JsonProperty("id")
    val id: String? = null,

    @field:JsonProperty("type")
    val type: String? = null
)