package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class AMapPoiResp(

    @field:JsonProperty("pois")
    val pois: List<Poi>? = null
) : AMapListResp()