package top.bettercode.summer.tools.pay.weixin.entity

import top.bettercode.summer.tools.lang.util.RandomUtil
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class RefundRequest @JvmOverloads constructor(

        /**
         * 商户订单号；非必填；商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号 transaction_id、out_trade_no二选一，如果同时存在优先级：transaction_id> out_trade_no；示例：1217752501201407033233368018
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 商户退款单号；必填；商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔；示例：1217752501201407033233368018
         */
        @field:JsonProperty("out_refund_no")
        var outRefundNo: String? = null,
        /**
         * 订单金额；必填；订单总金额，单位为分，只能为整数，详见支付金额；示例：100
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 退款金额；必填；退款总金额，订单总金额，单位为分，只能为整数，详见支付金额；示例：100
         */
        @field:JsonProperty("refund_fee")
        var refundFee: Int? = null,
        /**
         * 退款原因；非必填；若商户传入，会在下发给用户的退款消息中体现退款原因 注意：若订单退款金额≤1元，且属于部分退款，则不会在退款消息中体现退款原因；示例：商品已售完
         */
        @field:JsonProperty("refund_desc")
        var refundDesc: String? = null,
        /**
         * 退款结果通知url；非必填；异步接收微信支付退款结果通知的回调地址，通知URL必须为外网可访问的url，不允许带参数 公网域名必须为https，如果是走专线接入，使用专线NAT IP或者私有回调域名可使用http。 如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效；示例：https://weixin.qq.com/notify/
         */
        @field:JsonProperty("notify_url")
        var notifyUrl: String? = null,
        /**
         * 微信支付订单号；非必填；微信生成的订单号，在支付通知中有返回；示例：1217752501201407033233368018
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 公众账号ID；必填；微信分配的公众账号ID（企业号corpid即为此appId）；示例：wx8888888888888888
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：1900000109
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位。推荐随机数生成算法；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = RandomUtil.nextString2(32),
        /**
         * 签名；必填；签名，详见签名生成算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 签名类型；非必填；签名类型，目前支持HMAC-SHA256和MD5，默认为MD5；示例：HMAC-SHA256
         */
        @field:JsonProperty("sign_type")
        var signType: String? = null,
        /**
         * 退款货币种类；非必填；退款货币类型，需与支付一致，或者不填。符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型；示例：CNY
         */
        @field:JsonProperty("refund_fee_type")
        var refundFeeType: String? = null,
        /**
         * 退款资金来源；非必填；仅针对老资金流商户使用 REFUND_SOURCE_UNSETTLED_FUNDS---未结算资金退款（默认使用未结算资金退款） REFUND_SOURCE_RECHARGE_FUNDS---可用余额退款；示例：REFUND_SOURCE_RECHARGE_FUNDS
         */
        @field:JsonProperty("refund_account")
        var refundAccount: String? = null,
)
