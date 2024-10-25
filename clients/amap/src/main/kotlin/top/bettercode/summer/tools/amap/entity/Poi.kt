package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Poi(

    @field:JsonProperty("poiweight")
    val poiweight: String? = null,

    @field:JsonProperty("businessarea")
    val businessarea: String? = null,

    @field:JsonProperty("address")
    val address: String? = null,

    @field:JsonProperty("distance")
    val distance: String? = null,

    @field:JsonProperty("name")
    val name: String? = null,

    @field:JsonProperty("tel")
    val tel: String? = null,

    @field:JsonProperty("location")
    val location: String? = null,

    @field:JsonProperty("id")
    val id: String? = null,

    @field:JsonProperty("type")
    val type: String? = null,

    @field:JsonProperty("direction")
    val direction: String? = null
)