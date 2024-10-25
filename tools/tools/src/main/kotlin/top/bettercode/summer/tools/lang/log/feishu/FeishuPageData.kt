package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
class FeishuPageData<T> {
    var items: List<T>? = null
    @JsonProperty("page_token")
    var pageToken: String? = null
    @JsonProperty("has_more")
    var hasMore: Boolean? = null
}