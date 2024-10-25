package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
class FeishuTokenResult : FeishuResult() {
    var expire: Int? = null
    @JsonProperty("tenant_access_token")
    var tenantAccessToken: String? = null
}





