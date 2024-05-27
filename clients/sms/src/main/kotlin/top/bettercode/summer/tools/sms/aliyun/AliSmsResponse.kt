package top.bettercode.summer.tools.sms.aliyun

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.client.ClientResponse

open class AliSmsResponse : ClientResponse {
    @JsonProperty("RequestId")
    var requestId: String? = null

    @JsonProperty("BizId")
    var bizId: String? = null

    @JsonProperty("Code")
    var code: String? = null

    @JsonProperty("Message")
    override var message: String? = null
        get() {
            val regex = "触发号码天级流控Permits:(\\d+)"
            var message = field
            if (message?.matches(Regex(regex)) == true) {
                message =
                    "每个手机号一天只能获取" + message.replace(regex.toRegex(), "$1") + "条短信"
            }
            return message
        }

    override val isOk: Boolean
        //--------------------------------------------
        get() = "OK" == code

}
