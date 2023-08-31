import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse

/**
 * @author Peter Wu
 */
data class OrderQueryResponse(

        /**
         * 应用APPID，微信开放平台审核通过的应用APPID
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
         * 业务结果，SUCCESS/FAIL
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码，错误码
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述，结果信息描述
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
        /**
         * 设备号，微信支付分配的终端设备号，
         */
        @field:JsonProperty("device_info")
        var deviceInfo: String? = null,
        /**
         * 用户标识，用户在商户appid下的唯一标识
         */
        @field:JsonProperty("openid")
        var openid: String? = null,
        /**
         * 是否关注公众账号，已废弃，默认统一返回N
         */
        @field:JsonProperty("is_subscribe")
        var isSubscribe: String? = null,
        /**
         * 交易类型，调用接口提交的交易类型
         */
        @field:JsonProperty("trade_type")
        var tradeType: String? = null,
        /**
         * 交易状态，SUCCESS--支付成功 REFUND--转入退款 NOTPAY--未支付 CLOSED--已关闭 REVOKED--已撤销(刷卡支付) USERPAYING--用户支付中 PAYERROR--支付失败(其他原因，如银行返回失败) ACCEPT--已接收，等待扣款
         */
        @field:JsonProperty("trade_state")
        var tradeState: String? = null,
        /**
         * 付款银行，银行类型，采用字符串类型的银行标识
         */
        @field:JsonProperty("bank_type")
        var bankType: String? = null,
        /**
         * 总金额，订单总金额，单位为分
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 货币种类，货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
         */
        @field:JsonProperty("fee_type")
        var feeType: String? = null,
        /**
         * 现金支付金额，现金支付金额订单现金支付金额，详见支付金额
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
         * 代金券金额，“代金券或立减优惠”金额<=订单总金额，订单总金额-“代金券或立减优惠”金额=现金支付金额，详见支付金额
         */
        @field:JsonProperty("coupon_fee")
        var couponFee: Int? = null,
        /**
         * 代金券使用数量，代金券或立减优惠使用数量
         */
        @field:JsonProperty("coupon_count")
        var couponCount: Int? = null,
        /**
         * 微信支付订单号，微信支付订单号
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 商户订单号，商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 附加数据，附加数据，原样返回
         */
        @field:JsonProperty("attach")
        var attach: String? = null,
        /**
         * 支付完成时间，订单支付时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
         */
        @field:JsonProperty("time_end")
        var timeEnd: String? = null,
        /**
         * 交易状态描述，对当前查询订单状态的描述和下一步操作的指引
         */
        @field:JsonProperty("trade_state_desc")
        var tradeStateDesc: String? = null,
        /**
         * 其他
         */
        @field:JsonAnyGetter
        @field:JsonAnySetter
        var other: Map<String, Any?>? = null

) : WeixinPayResponse() {

    override fun isBizOk(): Boolean {
        return "SUCCESS" == resultCode && "SUCCESS" == tradeState
    }
}
