package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Distance(

    @field:JsonProperty("duration")
    val duration: String? = null,

    @field:JsonProperty("distance")
    val distance: String? = null,

    @field:JsonProperty("origin_id")
    val originId: String? = null,

    @field:JsonProperty("dest_id")
    val destId: String? = null
)
