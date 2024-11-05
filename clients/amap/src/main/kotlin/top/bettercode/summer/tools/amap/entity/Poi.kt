package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Poi(

    @field:JsonProperty("parent")
    val parent: String? = null,

    @field:JsonProperty("address")
    val address: String? = null,

    @field:JsonProperty("distance")
    val distance: String? = null,

    @field:JsonProperty("pcode")
    val pcode: String? = null,

    @field:JsonProperty("adcode")
    val adcode: String? = null,

    @field:JsonProperty("pname")
    val pname: String? = null,

    @field:JsonProperty("cityname")
    val cityname: String? = null,

    @field:JsonProperty("type")
    val type: String? = null,

    @field:JsonProperty("typecode")
    val typecode: String? = null,

    @field:JsonProperty("adname")
    val adname: String? = null,

    @field:JsonProperty("citycode")
    val citycode: String? = null,

    @field:JsonProperty("name")
    val name: String? = null,

    @field:JsonProperty("location")
    val location: String? = null,

    @field:JsonProperty("id")
    val id: String? = null
)
