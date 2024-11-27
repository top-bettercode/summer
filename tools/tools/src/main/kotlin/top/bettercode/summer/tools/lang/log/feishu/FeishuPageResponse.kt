package top.bettercode.summer.tools.lang.log.feishu

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * @author Peter Wu
 */
class FeishuPageResponse<T> {
    /**
     * 查询的雇佣信息
     */
    @JsonProperty("items")
    var items: List<T>? = null

    /**
     * 分页标记，当 has_more 为 true 时，会同时返回新的 page_token，否则不返回 page_token
     */
    @JsonProperty("page_token")
    var pageToken: String? = null

    /**
     * 是否还有更多项
     */
    @JsonProperty("has_more")
    var hasMore: Boolean? = null
}