package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class AMapGeoResp(

    @field:JsonProperty("count")
    val count: String? = null,

    @field:JsonProperty("geocodes")
    val geocodes: List<Geocode>? = null
) : AMapResp() {

    @get:JsonIgnore
    val hasGeocodes: Boolean
        get() = (count?.toInt() ?: 0) > 0
}