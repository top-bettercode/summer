package top.bettercode.summer.tools.pay.weixin.entity

import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class TransfersResponse(

        /**
         * 商户appid；必填；申请商户号的appid或商户号绑定的appid（号corpid即为此appId）；示例：wx8888888888888888
         */
        @field:JsonProperty("mch_appid")
        var mchAppid: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：1900000109
         */
        @field:JsonProperty("mchid")
        var mchid: String? = null,
        /**
         * 设备号；非必填；微信支付分配的终端设备号；示例：013467007045764
         */
        @field:JsonProperty("device_info")
        var deviceInfo: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 业务结果；必填；SUCCESS/FAIL，注意：当状态为FAIL时，存在业务结果未明确的情况。如果状态为FAIL，请务必关注错误代码（err_code字段），通过查询接口确认此次付款的结果；示例：SUCCESS
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码；非必填；错误码信息，注意：出现未明确的错误码时（SYSTEMERROR等），请务必用原商户订单号重试，或通过查询接口确认此次付款的结果；示例：SYSTEMERROR
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述；非必填；结果信息描述；示例：系统错误
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
        /**
         * 商户订单号；必填；商户订单号，需保持历史全局唯一性(只能是字母或者数字，不能包含有其它字符)；示例：1217752501201407033233368018
         */
        @field:JsonProperty("partner_trade_no")
        var partnerTradeNo: String? = null,
        /**
         * 微信付款单号；必填；付款成功，返回的微信付款单号；示例：1007752501201407033233368018
         */
        @field:JsonProperty("payment_no")
        var paymentNo: String? = null,
        /**
         * 付款成功时间；必填；付款成功时间；示例：2015-05-1915:26:59
         */
        @field:JsonProperty("payment_time")
        var paymentTime: String? = null,
) : WeixinPayResponse() {

    override fun isBizOk(): Boolean {
        return "SUCCESS" == resultCode
    }
}
