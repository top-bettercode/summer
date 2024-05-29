package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.client.ClientResponse

@JsonIgnoreProperties(ignoreUnknown = true)
data class AMapRegeo(
    @field:JsonProperty("status")
    val status: String? = null,

    @field:JsonProperty("regeocode")
    val regeocode: Any? = null,

    @field:JsonProperty("info")
    val info: String? = null,

    @field:JsonProperty("infocode")
    val infocode: String? = null
) : ClientResponse {

    @get:JsonIgnore
    override val message: String?
        get() = info

    @get:JsonIgnore
    override val isOk: Boolean by lazy { "1" == status }
}