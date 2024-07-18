package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FeishuChat(
    @JsonProperty("chat_id")
    var chatId: String? = null,
    var avatar: String? = null,
    var name: String? = null,
    var description: String? = null,
    @JsonProperty("owner_id")
    var ownerId: String? = null,
    @JsonProperty("owner_id_type")
    var ownerIdType: String? = null,
    var external: Boolean? = null,
    @JsonProperty("tenant_key")
    var tenantKey: String? = null,
    @JsonProperty("chat_status")
    var chatStatus: String? = null
){

    override fun toString(): String {
        return "(name=$name, description=$description)"
    }
}