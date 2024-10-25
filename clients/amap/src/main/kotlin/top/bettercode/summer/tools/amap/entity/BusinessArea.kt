package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class BusinessArea(

    @field:JsonProperty("businessArea")
    val businessArea: String? = null,

    @field:JsonProperty("location")
    val location: String? = null,

    @field:JsonProperty("name")
    val name: String? = null,

    @field:JsonProperty("id")
    val id: String? = null
)