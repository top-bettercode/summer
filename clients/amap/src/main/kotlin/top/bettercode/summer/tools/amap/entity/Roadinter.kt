package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Roadinter(

    @field:JsonProperty("second_id")
    val secondId: String? = null,

    @field:JsonProperty("first_id")
    val firstId: String? = null,

    @field:JsonProperty("distance")
    val distance: String? = null,

    @field:JsonProperty("second_name")
    val secondName: String? = null,

    @field:JsonProperty("location")
    val location: String? = null,

    @field:JsonProperty("first_name")
    val firstName: String? = null,

    @field:JsonProperty("direction")
    val direction: String? = null
)