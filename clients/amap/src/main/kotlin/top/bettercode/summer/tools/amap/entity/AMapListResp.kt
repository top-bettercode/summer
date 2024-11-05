package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

open class AMapListResp(

    @field:JsonProperty("count")
    val count: String? = null,

    ) : AMapResp() {

    @get:JsonIgnore
    val isNotEmpty: Boolean by lazy {
        (count?.toInt() ?: 0) > 0
    }
}