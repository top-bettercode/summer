package top.bettercode.summer.tools.pay.weixin.entity


import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class RefundInfo(

        /**
         * 微信订单号；必填；微信订单号；示例：1217752501201407033233368018
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 商户订单号；必填；商户系统内部的订单号；示例：1217752501201407033233368018
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 微信退款单号；必填；微信退款单号；示例：1217752501201407033233368018
         */
        @field:JsonProperty("refund_id")
        var refundId: String? = null,
        /**
         * 商户退款单号；必填；商户退款单号；示例：1217752501201407033233368018
         */
        @field:JsonProperty("out_refund_no")
        var outRefundNo: String? = null,
        /**
         * 订单金额；必填；订单总金额，单位为分，只能为整数，详见支付金额；示例：100
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 应结订单金额；非必填；当该订单有使用非充值券时，返回此字段。应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额；示例：100
         */
        @field:JsonProperty("settlement_total_fee")
        var settlementTotalFee: Int? = null,
        /**
         * 申请退款金额；必填；退款总金额,单位为分；示例：100
         */
        @field:JsonProperty("refund_fee")
        var refundFee: Int? = null,
        /**
         * 退款金额；必填；退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额；示例：100
         */
        @field:JsonProperty("settlement_refund_fee")
        var settlementRefundFee: Int? = null,
        /**
         * 退款状态；必填；SUCCESS-退款成功 CHANGE-退款异常 REFUNDCLOSE—退款关闭；示例：SUCCESS
         */
        @field:JsonProperty("refund_status")
        var refundStatus: String? = null,
        /**
         * 退款成功时间；非必填；09:46:01 资金退款至用户账号的时间，格式2017-12-15 09:46:01；示例：2017-12-15
         */
        @field:JsonProperty("success_time")
        var successTime: String? = null,
        /**
         * 退款入账账户；必填；取当前退款单的退款入账方 1）退回银行卡： {银行名称}{卡类型}{卡尾号} 2）退回支付用户零钱: 支付用户零钱 3）退还商户: 商户基本账户 商户结算银行账户 4）退回支付用户零钱通: 支付用户零钱通；示例：招商银行信用卡0403
         */
        @field:JsonProperty("refund_recv_accout")
        var refundRecvAccout: String? = null,
        /**
         * 退款资金来源；必填；REFUND_SOURCE_RECHARGE_FUNDS 可用余额退款/基本账户 REFUND_SOURCE_UNSETTLED_FUNDS 未结算资金退款；示例：REFUND_SOURCE_RECHARGE_FUNDS
         */
        @field:JsonProperty("refund_account")
        var refundAccount: String? = null,
        /**
         * 退款发起来源；必填；API接口 VENDOR_PLATFORM商户平台；示例：API
         */
        @field:JsonProperty("refund_request_source")
        var refundRequestSource: String? = null,
        /**
         * 用户退款金额；必填；退款给用户的金额，不包含所有优惠券金额；示例：90
         */
        @field:JsonProperty("cash_refund_fee")
        var cashRefundFee: Int? = null,
) {
    fun isRefundOk(): Boolean {
        return refundStatus == "SUCCESS"
    }
}
