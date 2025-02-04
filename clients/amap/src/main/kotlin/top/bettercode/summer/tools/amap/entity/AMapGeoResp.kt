package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class AMapGeoResp(
    @field:JsonProperty("geocodes")
    val geocodes: List<Geocode>? = null
) : AMapListResp()