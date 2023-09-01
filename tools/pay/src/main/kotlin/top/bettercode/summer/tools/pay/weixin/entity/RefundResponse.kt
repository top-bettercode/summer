package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse

/**
 * @author Peter Wu
 */
data class RefundResponse(

        /**
         * 业务结果；必填；SUCCESS/FAIL SUCCESS退款申请接收成功，结果通过退款查询接口查询 FAIL 提交业务失败；示例：SUCCESS
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码；非必填；列表详见错误码列表；示例：SYSTEMERROR
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述；非必填；结果信息描述；示例：系统超时
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
        /**
         * 公众账号ID；必填；微信分配的公众账号ID；示例：wx8888888888888888
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：1900000109
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 签名；必填；签名，详见签名算法；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 微信支付订单号；必填；微信订单号；示例：4007752501201407033233368018
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 商户订单号；必填；商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号；示例：33368018
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 商户退款单号；必填；商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔；示例：121775250
         */
        @field:JsonProperty("out_refund_no")
        var outRefundNo: String? = null,
        /**
         * 微信退款单号；必填；微信退款单号；示例：2007752501201407033233368018
         */
        @field:JsonProperty("refund_id")
        var refundId: String? = null,
        /**
         * 退款金额；必填；退款总金额,单位为分,可以做部分退款；示例：100
         */
        @field:JsonProperty("refund_fee")
        var refundFee: Int? = null,
        /**
         * 应结退款金额；非必填；去掉非充值代金券退款金额后的退款金额，退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额；示例：100
         */
        @field:JsonProperty("settlement_refund_fee")
        var settlementRefundFee: Int? = null,
        /**
         * 标价金额；必填；订单总金额，单位为分，只能为整数，详见支付金额；示例：100
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 应结订单金额；非必填；去掉非充值代金券金额后的订单总金额，应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额；示例：100
         */
        @field:JsonProperty("settlement_total_fee")
        var settlementTotalFee: Int? = null,
        /**
         * 标价币种；非必填；订单金额货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型；示例：CNY
         */
        @field:JsonProperty("fee_type")
        var feeType: String? = null,
        /**
         * 现金支付金额；必填；现金支付金额，单位为分，只能为整数，详见支付金额；示例：100
         */
        @field:JsonProperty("cash_fee")
        var cashFee: Int? = null,
        /**
         * 现金支付币种；非必填；货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型；示例：CNY
         */
        @field:JsonProperty("cash_fee_type")
        var cashFeeType: String? = null,
        /**
         * 现金退款金额；非必填；现金退款金额，单位为分，只能为整数，详见支付金额；示例：100
         */
        @field:JsonProperty("cash_refund_fee")
        var cashRefundFee: Int? = null,
        /**
         * 代金券退款总金额；非必填；代金券退款金额<=退款金额，退款金额-代金券或立减优惠退款金额为现金，说明详见代金券或立减优惠；示例：100
         */
        @field:JsonProperty("coupon_refund_fee")
        var couponRefundFee: Int? = null,
        /**
         * 退款代金券使用数量；非必填；退款代金券使用数量；示例：1
         */
        @field:JsonProperty("coupon_refund_count")
        var couponRefundCount: Int? = null,
        /**
         * 其他
         */
        @get:JsonAnyGetter
        @field:JsonAnySetter
        var other: Map<String, Any?>? = null
        /**
         * 代金券类型；非必填；CASH--充值代金券 NO_CASH---非充值代金券 订单使用代金券时有返回（取值：CASH、NO_CASH）。$n为下标,从0开始编号，举例：coupon_type_0；示例：CASH
         */
        //@field:JsonProperty("coupon_type_$n")
        //var couponType$n: String? = null,
        /**
         * 单个代金券退款金额；非必填；代金券退款金额<=退款金额，退款金额-代金券或立减优惠退款金额为现金，说明详见代金券或立减优惠；示例：100
         */
        //@field:JsonProperty("coupon_refund_fee_$n")
        //var couponRefundFee$n: Int? = null,
        /**
         * 退款代金券ID；非必填；退款代金券ID, $n为下标，从0开始编号；示例：10000
         */
        //@field:JsonProperty("coupon_refund_id_$n")
        //var couponRefundId$n: String? = null,
) : WeixinPayResponse() {

    override fun isBizOk(): Boolean {
        return "SUCCESS" == resultCode
    }

    @JsonIgnore
    fun get(key: String): Any? {
        return other?.get(key)
    }

}
