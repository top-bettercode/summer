package top.bettercode.summer.util.mobile.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class QueryResponse(
    @field:JsonProperty("data")
    val data: List<DataItem?>? = null
) {
    val record: RecordItem?
        get() = data?.firstOrNull()?.record?.firstOrNull()

    val dataItem: DataItem?
        get() = data?.firstOrNull()

    fun isOk(): Boolean {
        return record?.resCode == "00"
    }

    val message: String?
        get() = record?.resDesc

    val mobile: String?
        get() = dataItem?.mobile
}