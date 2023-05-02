package top.bettercode.summer.tools.sms.aliyun

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.sms.SmsResponse

open class AliSmsResponse : SmsResponse {
    @JsonProperty("RequestId")
    var requestId: String? = null

    @JsonProperty("BizId")
    var bizId: String? = null

    @JsonProperty("Code")
    var code: String? = null

    @JsonProperty("Message")
    override var message: String? = null
    override val isOk: Boolean
        //--------------------------------------------
        get() = "OK" == code
}
