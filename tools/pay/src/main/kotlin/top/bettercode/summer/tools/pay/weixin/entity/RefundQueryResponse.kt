import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse

/**
 * @author Peter Wu
 */
data class RefundQueryResponse(

        /**
         * 业务结果，SUCCESS/FAIL SUCCESS退款申请接收成功，退款结果以退款状态为准 FAIL
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误码，错误码详见第6节
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误描述，结果信息描述
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
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
         * 微信订单号，微信订单号
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 商户订单号，商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 订单总退款次数，订单总共已发生的部分退款次数，当请求参数传入offset后有返回
         */
        @field:JsonProperty("total_refund_count")
        var totalRefundCount: Int? = null,
        /**
         * 订单总金额，订单总金额，单位为分，只能为整数，详见支付金额
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 订单金额货币种类，订单金额货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
         */
        @field:JsonProperty("fee_type")
        var feeType: String? = null,
        /**
         * 现金支付金额，现金支付金额，单位为分，只能为整数，详见支付金额
         */
        @field:JsonProperty("cash_fee")
        var cashFee: Int? = null,
        /**
         * 现金支付货币类型，货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
         */
        @field:JsonProperty("cash_fee_type")
        var cashFeeType: String? = null,
        /**
         * 应结订单金额，当订单使用了免充值型优惠券后返回该参数，应结订单金额=订单金额-免充值优惠券金额。
         */
        @field:JsonProperty("settlement_total_fee")
        var settlementTotalFee: Int? = null,
        /**
         * 退款笔数，当前返回退款笔数
         */
        @field:JsonProperty("refund_count")
        var refundCount: Int? = null,
        /**
         * 退款总金额，各退款单的退款金额累加
         */
        @field:JsonProperty("refund_fee")
        var refundFee: Int? = null,
        /**
         * 代金券退款总金额，各退款单的代金券退款金额累加
         */
        @field:JsonProperty("coupon_refund_fee")
        var couponRefundFee: Int? = null,
        /**
         * 用户退款金额，退款给用户的金额，不包含所有优惠券金额
         */
        @field:JsonProperty("cash_refund_fee")
        var cashRefundFee: Int? = null,
        /**
         * 其他
         */
        @field:JsonAnyGetter
        @field:JsonAnySetter
        var other: Map<String, Any?>? = null
        /**
         * 退款金额，退款总金额,单位为分,可以做部分退款
         */
        //@field:JsonProperty("refund_fee_$n")
        //var refundFee$n: Int? = null,
        /**
         * 代金券退款金额，代金券或立减优惠退款金额<=退款金额，退款金额-代金券或立减优惠退款金额为现金，说明详见代金券或立减优惠
         */
        //@field:JsonProperty("coupon_refund_fee_$n")
        //var couponRefundFee$n: Int? = null,
        /**
         * 代金券使用数量，代金券或立减优惠使用数量 ,$n为下标,从0开始编号
         */
        //@field:JsonProperty("coupon_refund_count_$n")
        //var couponRefundCount$n: Int? = null,
        /**
         * 代金券ID，代金券或立减优惠ID, $n为下标，$m为下标，从0开始编号
         */
        //@field:JsonProperty("coupon_refund_id_$n_$m")
        //var couponRefundId$n$m: String? = null,
        /**
         * 代金券类型，CASH--充值代金券 NO_CASH---非充值优惠券 开通免充值券功能，并且订单使用了优惠券后有返回（取值：CASH、NO_CASH）。$n为下标,$m为下标,从0开始编号，举例：coupon_type_$0_$1
         */
        //@field:JsonProperty("coupon_type_$n_$m")
        //var couponType$n$m: String? = null,
        /**
         * 单个代金券退款金额，单个代金券或立减优惠退款金额, $n为下标，$m为下标，从0开始编号
         */
        //@field:JsonProperty("coupon_refund_fee_$n_$m")
        //var couponRefundFee$n$m: Int? = null,
        /**
         * 退款状态，退款状态： SUCCESS—退款成功 REFUNDCLOSE—退款关闭，指商户发起退款失败的情况。 PROCESSING—退款处理中 CHANGE—退款异常，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，可前往商户平台（pay.weixin.qq.com）-交易中心，手动处理此笔退款。$n为下标，从0开始编号。
         */
        //@field:JsonProperty("refund_status_$n")
        //var refundStatus$n: String? = null,
        /**
         * 退款资金来源，REFUND_SOURCE_RECHARGE_FUNDS---可用余额退款/基本账户 REFUND_SOURCE_UNSETTLED_FUNDS---未结算资金退款 $n为下标，从0开始编号。
         */
        //@field:JsonProperty("refund_account_$n")
        //var refundAccount$n: String? = null,
        /**
         * 退款入账账户，取当前退款单的退款入账方 1）退回银行卡： {银行名称}{卡类型}{卡尾号} 2）退回支付用户零钱: 支付用户零钱 3）退还商户: 商户基本账户 商户结算银行账户 4）退回支付用户零钱通: 支付用户零钱通
         */
        //@field:JsonProperty("refund_recv_accout_$n")
        //var refundRecvAccout$n: String? = null,
        /**
         * 退款成功时间，退款成功时间，当退款状态为退款成功时有返回。$n为下标，从0开始编号。
         */
        //@field:JsonProperty("refund_success_time_$n")
        //var refundSuccessTime$n: String? = null,

        /**
         * 商户退款单号，商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔。
         */
        //@field:JsonProperty("out_refund_no_$n")
        //var outRefundNo$n: String? = null,
        /**
         * 微信退款单号，微信退款单号
         */
        //@field:JsonProperty("refund_id_$n")
        //var refundId$n: String? = null,
        /**
         * 退款渠道，ORIGINAL—原路退款 BALANCE—退回到余额 OTHER_BALANCE—原账户异常退到其他余额账户 OTHER_BANKCARD—原银行卡异常退到其他银行卡
         */
        //@field:JsonProperty("refund_channel_$n")
        //var refundChannel$n: String? = null,
) : WeixinPayResponse() {
    /**
     * 业务结果
     */
    override fun isBizOk(): Boolean {
        return resultCode == "SUCCESS"
    }
}
