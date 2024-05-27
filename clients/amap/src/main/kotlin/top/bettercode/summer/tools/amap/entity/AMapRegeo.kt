package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.client.ClientResponse

data class AMapRegeo(
    @field:JsonProperty("status")
    val status: String? = null,

    @field:JsonProperty("regeocode")
    val regeocode: Regeocode? = null,

    @field:JsonProperty("info")
    val info: String? = null,

    @field:JsonProperty("infocode")
    val infocode: String? = null
) : ClientResponse {

    override val message: String?
        get() = info

    override val isOk: Boolean by lazy { "1" == status }
}