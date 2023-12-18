package top.bettercode.summer.tools.sms.aliyun

import com.fasterxml.jackson.annotation.JsonProperty

class AliSendReportResponse : AliSmsResponse() {
    @JsonProperty("TotalCount")
    var totalCount: String? = null

    @JsonProperty("SmsSendDetailDTOs")
    var smsSendDetailDTOs: Map<String, List<AliSendReport>>? = null

    //--------------------------------------------
    class AliSendReport {
        /**
         * 接收短信的手机号码
         */
        @JsonProperty("PhoneNum")
        var phoneNum: String? = null

        /**
         * 短信发送状态，包括：
         *
         *
         * 1：等待回执。 2：发送失败。 3：发送成功。
         */
        @JsonProperty("SendStatus")
        var sendStatus: Long? = null

        /**
         * 运营商短信状态码。
         *
         *
         * 短信发送成功：DELIVERED。 短信发送失败：失败错误码请参见错误码。
         */
        @JsonProperty("ErrCode")
        var errCode: String? = null

        /**
         * 短信模板ID。
         */
        @JsonProperty("TemplateCode")
        var templateCode: String? = null

        /**
         * 短信内容
         */
        @JsonProperty("Content")
        var content: String? = null

        /**
         * 短信发送日期和时间
         */
        @JsonProperty("SendDate")
        var sendDate: String? = null

        /**
         * 短信接收日期和时间
         */
        @JsonProperty("ReceiveDate")
        var receiveDate: String? = null

        /**
         * 外部流水扩展字段
         */
        @JsonProperty("OutId")
        var outId: String? = null
    }
}
