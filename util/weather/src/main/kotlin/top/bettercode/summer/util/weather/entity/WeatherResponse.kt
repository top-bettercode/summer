package top.bettercode.summer.util.weather.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class WeatherResponse(

    @field:JsonProperty("success")
    val success: String? = null,

    @field:JsonProperty("msgid")
    val msgid: String? = null,

    @field:JsonProperty("msg")
    val msg: String? = null,

    @field:JsonProperty("result")
    val result: WeatherResult? = null

) {
    fun isOk() = success == "1"
}