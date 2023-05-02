package top.bettercode.summer.tools.sms.aliyun

import com.fasterxml.jackson.annotation.JsonProperty

class Reason {
    /**
     * 审批未通过的备注信息。
     */
    @JsonProperty("RejectSubInfo")
    var rejectSubInfo: String? = null

    /**
     * 审批未通过的时间，格式为yyyy-MM-dd HH:mm:ss。
     */
    @JsonProperty("RejectDate")
    var rejectDate: String? = null

    /**
     * 审批未通过的原因。
     */
    @JsonProperty("RejectInfo")
    var rejectInfo: String? = null
    override fun toString(): String {
        return "Reason{" +
                "rejectSubInfo = '" + rejectSubInfo + '\'' +
                ",rejectDate = '" + rejectDate + '\'' +
                ",rejectInfo = '" + rejectInfo + '\'' +
                "}"
    }
}