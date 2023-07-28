package top.bettercode.summer.tools.weather.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class WeatherType(

        @field:JsonProperty("wtId")
        val wtId: String? = null,

        @field:JsonProperty("wtNm")
        val wtNm: String? = null,

        @field:JsonProperty("icon")
        val icon: String? = null,

        @field:JsonProperty("oicon")
        val oicon: String? = null
)