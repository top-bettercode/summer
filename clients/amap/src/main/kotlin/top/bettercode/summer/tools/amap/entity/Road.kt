package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Road(

    @field:JsonProperty("distance")
    val distance: String? = null,

    @field:JsonProperty("name")
    val name: String? = null,

    @field:JsonProperty("location")
    val location: String? = null,

    @field:JsonProperty("id")
    val id: String? = null,

    @field:JsonProperty("direction")
    val direction: String? = null
)