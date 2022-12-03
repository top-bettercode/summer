package top.bettercode.summer.tools.mobile.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class DataItem(

    @field:JsonProperty("record")
	val record: List<RecordItem?>? = null,

    @field:JsonProperty("name")
	val name: String? = null,

    @field:JsonProperty("mobile")
	val mobile: String? = null,

    @field:JsonProperty("recordNum")
	val recordNum: Int? = null
)