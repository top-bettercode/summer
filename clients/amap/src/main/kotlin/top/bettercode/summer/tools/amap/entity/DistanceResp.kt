package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class DistanceResp(

    @field:JsonProperty("count")
    val count: String? = null,

    @field:JsonProperty("results")
    val results: List<Distance>? = null,

    ) : AMapResp()


