package top.bettercode.summer.tools.sms.aliyun

import com.fasterxml.jackson.annotation.JsonProperty

class AliSmsSignResponse : AliSmsResponse() {
    @JsonProperty("SmsSignList")
    var smsSignList: List<SmsSignListItem>? = null
    override fun toString(): String {
        return "AliSmsSignResp{" +
                "smsSignList=" + smsSignList +
                "} " + super.toString()
    }
}