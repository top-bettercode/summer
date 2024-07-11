package top.bettercode.summer.logging.feishu

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class FeishuPageData<T> {
    var items: List<T>? = null
    @JsonProperty("page_token")
    var pageToken: String? = null
    @JsonProperty("has_more")
    var hasMore: Boolean? = null
}