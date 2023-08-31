package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class RefundQueryRequest(
        /**
         * 商户退款单号，商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔。
         */
        @field:JsonProperty("out_refund_no")
        var outRefundNo: String? = null,

        /**
         * 商户订单号，商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,

        /**
         * 微信退款单号，微信生成的退款单号，在申请退款接口有返回
         */
        @field:JsonProperty("refund_id")
        var refundId: String? = null,
        /**
         * 微信订单号，微信订单号查询的优先级是： refund_id > out_refund_no > transaction_id > out_trade_no
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 应用ID，微信开放平台审核通过的应用APPID
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号，微信支付分配的商户号
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 随机字符串，随机字符串，不长于32位。推荐随机数生成算法
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 签名，签名，详见签名生成算法
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 偏移量，偏移量，当部分退款次数超过10次时可使用，表示返回的查询结果从这个偏移量开始取记录
         */
        @field:JsonProperty("offset")
        var offset: Int? = null,
)
