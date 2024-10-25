package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class AMapRegeoResp(
    @field:JsonProperty("regeocode")
    val regeocode: Regeocode? = null,
) : AMapResp()