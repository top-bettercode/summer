package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
data class FeishuChat(
    @JsonProperty("chat_id")
    var chatId: String? = null,
    @JsonProperty("avatar")
    var avatar: String? = null,
    @JsonProperty("name")
    var name: String? = null,
    @JsonProperty("description")
    var description: String? = null,
    @JsonProperty("owner_id")
    var ownerId: String? = null,
    @JsonProperty("owner_id_type")
    var ownerIdType: String? = null,
    @JsonProperty("external")
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