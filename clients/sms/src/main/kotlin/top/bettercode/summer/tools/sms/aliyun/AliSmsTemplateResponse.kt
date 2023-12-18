package top.bettercode.summer.tools.sms.aliyun

import com.fasterxml.jackson.annotation.JsonProperty

class AliSmsTemplateResponse : AliSmsResponse() {
    @JsonProperty("SmsTemplateList")
    var smsTemplateList: List<SmsTemplateListItem>? = null
    override fun toString(): String {
        return "AliSmsTemplateResponse{" +
                "smsTemplateList = '" + smsTemplateList + '\'' +
                "}"
    }
}