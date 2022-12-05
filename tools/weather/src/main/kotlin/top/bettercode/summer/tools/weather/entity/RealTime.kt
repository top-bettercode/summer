package top.bettercode.summer.tools.weather.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class RealTime(

    @field:JsonProperty("week")
    val week: String? = null,

    @field:JsonProperty("wtId")
    val wtId: String? = null,

    @field:JsonProperty("wtNm")
    val wtNm: String? = null,

    @field:JsonProperty("wtIcon")
    val wtIcon: String? = null,

    @field:JsonProperty("wtTemp")
    val wtTemp: String? = null,

    @field:JsonProperty("wtHumi")
    val wtHumi: String? = null,

    @field:JsonProperty("wtWindId")
    val wtWindId: String? = null,

    @field:JsonProperty("wtWindNm")
    val wtWindNm: String? = null,

    @field:JsonProperty("wtWinp")
    val wtWinp: String? = null,

    @field:JsonProperty("wtWins")
    val wtWins: String? = null,

    @field:JsonProperty("wtAqi")
    val wtAqi: String? = null,

    @field:JsonProperty("wtVisibility")
    val wtVisibility: String? = null,

    @field:JsonProperty("wtRainfall")
    val wtRainfall: String? = null,

    @field:JsonProperty("wtPressurel")
    val wtPressurel: String? = null
)