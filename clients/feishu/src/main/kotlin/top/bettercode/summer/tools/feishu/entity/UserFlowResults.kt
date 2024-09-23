package top.bettercode.summer.tools.feishu.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 打卡流水
 * @author Peter Wu
 */
class UserFlowResults {

    @JsonProperty("user_flow_results")
    var userFlowResults: List<UserFlow>? = null
}