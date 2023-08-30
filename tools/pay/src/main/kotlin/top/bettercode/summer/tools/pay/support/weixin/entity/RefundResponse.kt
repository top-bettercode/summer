package top.bettercode.summer.tools.pay.support.weixin.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.support.weixin.WeixinPayResponse

/**
 *
 * @author Peter Wu
 */
data class RefundResponse(

        /**
         * 业务结果，SUCCESS/FAIL SUCCESS退款申请接收成功，结果通过退款查询接口查询 FAIL 提交业务失败
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码，列表详见错误码列表
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述，结果信息描述
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
        /**
         * 公众账号ID，微信分配的公众账号ID
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号，微信支付分配的商户号
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 随机字符串，随机字符串，不长于32位
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 签名，签名，详见签名算法
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 微信支付订单号，微信订单号
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 商户订单号，商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 商户退款单号，商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔。
         */
        @field:JsonProperty("out_refund_no")
        var outRefundNo: String? = null,
        /**
         * 微信退款单号，微信退款单号
         */
        @field:JsonProperty("refund_id")
        var refundId: String? = null,
        /**
         * 退款金额，退款总金额,单位为分,可以做部分退款
         */
        @field:JsonProperty("refund_fee")
        var refundFee: Int? = null,
        /**
         * 应结退款金额，去掉非充值代金券退款金额后的退款金额，退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额
         */
        @field:JsonProperty("settlement_refund_fee")
        var settlementRefundFee: Int? = null,
        /**
         * 标价金额，订单总金额，单位为分，只能为整数，详见支付金额
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 应结订单金额，去掉非充值代金券金额后的订单总金额，应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。
         */
        @field:JsonProperty("settlement_total_fee")
        var settlementTotalFee: Int? = null,
        /**
         * 标价币种，订单金额货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
         */
        @field:JsonProperty("fee_type")
        var feeType: String? = null,
        /**
         * 现金支付金额，现金支付金额，单位为分，只能为整数，详见支付金额
         */
        @field:JsonProperty("cash_fee")
        var cashFee: Int? = null,
        /**
         * 现金支付币种，货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
         */
        @field:JsonProperty("cash_fee_type")
        var cashFeeType: String? = null,
        /**
         * 现金退款金额，现金退款金额，单位为分，只能为整数，详见支付金额
         */
        @field:JsonProperty("cash_refund_fee")
        var cashRefundFee: Int? = null,
        /**
         * 代金券退款总金额，代金券退款金额<=退款金额，退款金额-代金券或立减优惠退款金额为现金，说明详见代金券或立减优惠
         */
        @field:JsonProperty("coupon_refund_fee")
        var couponRefundFee: Int? = null,
        /**
         * 退款代金券使用数量，退款代金券使用数量
         */
        @field:JsonProperty("coupon_refund_count")
        var couponRefundCount: Int? = null,

        @field:JsonAnyGetter
        @field:JsonAnySetter
        var other: Map<String, Any?>? = null
) : WeixinPayResponse() {
    /**
     * 业务结果
     */
    override fun isBizOk(): Boolean {
        return resultCode == "SUCCESS"
    }
}