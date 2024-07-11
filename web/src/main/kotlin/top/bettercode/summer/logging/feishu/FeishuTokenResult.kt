package top.bettercode.summer.logging.feishu

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class FeishuTokenResult : FeishuResult() {
    var expire: Int? = null
    @JsonProperty("tenant_access_token")
    var tenantAccessToken: String? = null
}





