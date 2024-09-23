package top.bettercode.summer.tools.feishu.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 */
data class UserFlowRequest(
    /**
     * employee_no 必填
     */
    @JsonProperty("user_ids")
    var userIds: Array<String>? = null,

    /**
     * 查询的起始时间，秒级时间戳。示例值："1566641088" 必填
     */
    @JsonProperty("check_time_from")
    var checkTimeFrom: String? = null,

    /**
     * 查询的结束时间，秒级时间戳。示例值："1566641088" 必填
     */
    @JsonProperty("check_time_to")
    var checkTimeTo: String? = null
)
