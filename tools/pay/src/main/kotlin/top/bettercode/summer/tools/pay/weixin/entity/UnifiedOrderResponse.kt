package top.bettercode.summer.tools.pay.weixin.response

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse

data class UnifiedOrderResponse(
        /**
         * 应用APPID，调用接口提交的应用ID
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号，调用接口提交的商户号
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 设备号，调用接口提交的终端设备号，
         */
        @field:JsonProperty("device_info")
        var deviceInfo: String? = null,
        /**
         * 随机字符串，微信返回的随机字符串
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 签名，微信返回的签名，详见签名算法
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 业务结果，SUCCESS/FAIL
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码，详细参见第6节错误列表
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述，错误返回的信息描述
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
        /**
         * 交易类型，调用接口提交的交易类型，取值如下：JSAPI，NATIVE，APP，详细说明见参数规定
         */
        @field:JsonProperty("trade_type")
        var tradeType: String? = null,
        /**
         * 预支付交易会话标识，微信生成的预支付会话标识，用于后续接口调用中使用，该值有效期为2小时,针对H5支付此参数无特殊用途
         */
        @field:JsonProperty("prepay_id")
        var prepayId: String? = null
) : WeixinPayResponse() {

    /**
     * 业务结果是否成功
     */
    @JsonIgnore
   override fun isBizOk(): Boolean {
        return resultCode == "SUCCESS"
    }
}