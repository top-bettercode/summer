package top.bettercode.summer.tools.weather.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class WeatherResponse<T>(

    @field:JsonProperty("success")
    val success: String? = null,

    @field:JsonProperty("msgid")
    val msgid: String? = null,

    @field:JsonProperty("msg")
    val msg: String? = null,

    @field:JsonProperty("result")
    val result: T? = null

) {
    fun isOk() = success == "1"
}