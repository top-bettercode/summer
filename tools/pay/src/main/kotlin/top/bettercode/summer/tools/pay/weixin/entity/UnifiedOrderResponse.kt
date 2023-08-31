package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse

/**
 * @author Peter Wu
 */
data class UnifiedOrderResponse(

        /**
         * 应用APPID；必填；调用接口提交的应用ID；示例：wx8888888888888888
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号；必填；调用接口提交的商户号；示例：1900000109
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 设备号；非必填；调用接口提交的终端设备号；示例：013467007045764
         */
        @field:JsonProperty("device_info")
        var deviceInfo: String? = null,
        /**
         * 随机字符串；必填；微信返回的随机字符串；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 签名；必填；微信返回的签名，详见签名算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 业务结果；必填；SUCCESS/FAIL；示例：SUCCESS
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码；非必填；详细参见第6节错误列表；示例：SYSTEMERROR
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述；非必填；错误返回的信息描述；示例：系统错误
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
        /**
         * 交易类型；必填；调用接口提交的交易类型，取值如下：JSAPI，NATIVE，APP，详细说明见参数规定；示例：JSAPI
         */
        @field:JsonProperty("trade_type")
        var tradeType: String? = null,
        /**
         * 预支付交易会话标识；必填；微信生成的预支付会话标识，用于后续接口调用中使用，该值有效期为2小时,针对H5支付此参数无特殊用途；示例：wx201410272009395522657a690389285100
         */
        @field:JsonProperty("prepay_id")
        var prepayId: String? = null,
) : WeixinPayResponse() {

    override fun isBizOk(): Boolean {
        return "SUCCESS" == resultCode
    }
}
