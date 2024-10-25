package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Building(

    @field:JsonProperty("name")
    val name: String? = null,

    @field:JsonProperty("type")
    val type: String? = null
)