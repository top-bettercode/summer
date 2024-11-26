package top.bettercode.summer.tools.feishu.entity.userflow

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 打卡流水
 * @author Peter Wu
 */
class UserFlowCreateResults {

    @JsonProperty("flow_records")
    var flow_records: List<UserFlow>? = null
}