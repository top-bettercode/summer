package top.bettercode.summer.tools.amap.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.client.ClientResponse

data class Distance(

    @field:JsonProperty("count")
    val count: String? = null,

    @field:JsonProperty("infocode")
    val infocode: String? = null,

    @field:JsonProperty("results")
    val results: List<ResultsItem>? = null,

    @field:JsonProperty("status")
    val status: String? = null,

    @field:JsonProperty("info")
    val info: String? = null
) : ClientResponse {

    override val message: String?
        @JsonIgnore
        get() = info

    @get:JsonIgnore
    override val isOk: Boolean by lazy { "1" == status }
}


