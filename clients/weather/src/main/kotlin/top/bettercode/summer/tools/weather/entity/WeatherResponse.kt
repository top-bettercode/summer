package top.bettercode.summer.tools.weather.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.client.ClientResponse

data class WeatherResponse<T>(

    @field:JsonProperty("success")
    val success: String? = null,

    @field:JsonProperty("msgid")
    val msgid: String? = null,

    @field:JsonProperty("msg")
    val msg: String? = null,

    @field:JsonProperty("result")
    val result: T? = null

) : ClientResponse {

    override val isOk: Boolean
        get() = success == "1"

    override val message: String?
        get() = msg
}