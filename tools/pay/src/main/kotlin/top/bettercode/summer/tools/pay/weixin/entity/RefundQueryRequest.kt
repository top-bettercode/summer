package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.util.RandomUtil

/**
 * @author Peter Wu
 */
data class RefundQueryRequest @JvmOverloads constructor(

        /**
         * 商户退款单号；非必填；商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔；示例：1217752501201407033233368018
         */
        @field:JsonProperty("out_refund_no")
        var outRefundNo: String? = null,
        /**
         * 商户订单号；非必填；商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号；示例：1217752501201407033233368018
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 微信订单号；非必填；微信订单号查询的优先级是： refund_id > out_refund_no > transaction_id > out_trade_no；示例：1217752501201407033233368018
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 微信退款单号；非必填；微信生成的退款单号，在申请退款接口有返回；示例：1217752501201407033233368018
         */
        @field:JsonProperty("refund_id")
        var refundId: String? = null,
        /**
         * 应用ID；必填；微信开放平台审核通过的应用APPID；示例：wx8888888888888888
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
         * 偏移量；非必填；偏移量，当部分退款次数超过10次时可使用，表示返回的查询结果从这个偏移量开始取记录；示例：15
         */
        @field:JsonProperty("offset")
        var offset: Int? = null,
)
