package top.bettercode.summer.util.weather.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class WeatherResult(

	@field:JsonProperty("weaid")
	val weaid: String? = null,

	@field:JsonProperty("cityid")
	val cityid: String? = null,

	@field:JsonProperty("area_1")
	val area1: String? = null,

	@field:JsonProperty("area_2")
	val area2: String? = null,

	@field:JsonProperty("area_3")
	val area3: String? = null,

	@field:JsonProperty("realTime")
	val realTime: RealTime? = null
)