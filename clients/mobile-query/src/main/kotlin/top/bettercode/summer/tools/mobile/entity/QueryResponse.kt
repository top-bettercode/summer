package top.bettercode.summer.tools.mobile.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.client.ClientResponse

data class QueryResponse(
    @field:JsonProperty("data")
    val data: List<DataItem?>? = null
) : ClientResponse {
    val record: RecordItem?
        get() = data?.firstOrNull()?.record?.firstOrNull()

    val dataItem: DataItem?
        get() = data?.firstOrNull()

    override val isOk: Boolean
        get() {
            return record?.resCode == "00"
        }

    override val message: String?
        get() = record?.resDesc

    val mobile: String?
        get() = dataItem?.mobile
}