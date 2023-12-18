package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class AMapGeo(

        @field:JsonProperty("status")
        val status: String? = null,

        @field:JsonProperty("info")
        val info: String? = null,

        @field:JsonProperty("infocode")
        val infocode: String? = null,

        @field:JsonProperty("count")
        val count: String? = null,

        @field:JsonProperty("geocodes")
        val geocodes: List<GeocodesItem>? = null
) {
    val isOk: Boolean by lazy { "1" == status }
}