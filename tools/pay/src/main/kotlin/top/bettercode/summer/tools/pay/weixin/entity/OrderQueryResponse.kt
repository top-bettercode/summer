package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class OrderQueryResponse(

        /**
         * 应用APPID；必填；微信开放平台审核通过的应用APPID；示例：wxd678efh567hg6787
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：1230000109
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位。推荐随机数生成算法；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 签名；必填；签名，详见签名生成算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 业务结果；必填；SUCCESS/FAIL；示例：SUCCESS
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码；非必填；错误码；示例：SYSTEMERROR
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述；非必填；结果信息描述；示例：系统错误
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
        /**
         * 设备号；非必填；微信支付分配的终端设备号；示例：013467007045764
         */
        @field:JsonProperty("device_info")
        var deviceInfo: String? = null,
        /**
         * 用户标识；必填；用户在商户appid下的唯一标识；示例：oUpF8uMuAJO_M2pxb1Q9zNjWeS6o
         */
        @field:JsonProperty("openid")
        var openid: String? = null,
        /**
         * 是否关注公众账号；必填；已废弃，默认统一返回N；示例：N
         */
        @field:JsonProperty("is_subscribe")
        var isSubscribe: String? = null,
        /**
         * 交易类型；必填；调用接口提交的交易类型；示例：APP
         */
        @field:JsonProperty("trade_type")
        var tradeType: String? = null,
        /**
         * 交易状态；必填；SUCCESS--支付成功 REFUND--转入退款 NOTPAY--未支付 CLOSED--已关闭 REVOKED--已撤销(刷卡支付) USERPAYING--用户支付中 PAYERROR--支付失败(其他原因，如银行返回失败) ACCEPT--已接收，等待扣款；示例：SUCCESS
         */
        @field:JsonProperty("trade_state")
        var tradeState: String? = null,
        /**
         * 付款银行；必填；银行类型，采用字符串类型的银行标识；示例：CMC
         */
        @field:JsonProperty("bank_type")
        var bankType: String? = null,
        /**
         * 总金额；必填；订单总金额，单位为分；示例：100
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 货币种类；非必填；货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型；示例：CNY
         */
        @field:JsonProperty("fee_type")
        var feeType: String? = null,
        /**
         * 现金支付金额；必填；现金支付金额订单现金支付金额，详见支付金额；示例：100
         */
        @field:JsonProperty("cash_fee")
        var cashFee: Int? = null,
        /**
         * 现金支付货币类型；非必填；货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型；示例：CNY
         */
        @field:JsonProperty("cash_fee_type")
        var cashFeeType: String? = null,
        /**
         * 应结订单金额；非必填；当订单使用了免充值型优惠券后返回该参数，应结订单金额=订单金额-免充值优惠券金额；示例：100
         */
        @field:JsonProperty("settlement_total_fee")
        var settlementTotalFee: Int? = null,
        /**
         * 代金券金额；非必填；“代金券或立减优惠”金额<=订单总金额，订单总金额-“代金券或立减优惠”金额=现金支付金额，详见支付金额；示例：100
         */
        @field:JsonProperty("coupon_fee")
        var couponFee: Int? = null,
        /**
         * 代金券使用数量；非必填；代金券或立减优惠使用数量；示例：1
         */
        @field:JsonProperty("coupon_count")
        var couponCount: Int? = null,
        /**
         * 微信支付订单号；必填；微信支付订单号；示例：1009660380201506130728806387
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 商户订单号；必填；商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号；示例：20150806125346
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 附加数据；非必填；附加数据，原样返回；示例：深圳分店
         */
        @field:JsonProperty("attach")
        var attach: String? = null,
        /**
         * 支付完成时间；必填；订单支付时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则；示例：20141030133525
         */
        @field:JsonProperty("time_end")
        var timeEnd: String? = null,
        /**
         * 交易状态描述；必填；对当前查询订单状态的描述和下一步操作的指引；示例：支付失败，请重新下单支付
         */
        @field:JsonProperty("trade_state_desc")
        var tradeStateDesc: String? = null,
        /**
         * 其他
         */
        @field:JsonAnyGetter
        @field:JsonAnySetter
        var other: Map<String, Any?>? = null
        /**
         * 代金券ID；非必填；代金券或立减优惠ID, $n为下标，从0开始编号；示例：10000
         */
        //@field:JsonProperty("coupon_id_$n")
        //var couponId$n: String? = null,
        /**
         * 代金券类型；非必填；CASH--充值代金券 NO_CASH---非充值优惠券 开通免充值券功能，并且订单使用了优惠券后有返回（取值：CASH、NO_CASH）。$n为下标,从0开始编号，举例：coupon_type_$0；示例：CASH
         */
        //@field:JsonProperty("coupon_type_$n")
        //var couponType$n: String? = null,
        /**
         * 单个代金券支付金额；非必填；单个代金券或立减优惠支付金额, $n为下标，从0开始编号；示例：100
         */
        //@field:JsonProperty("coupon_fee_$n")
        //var couponFee$n: Int? = null,
) : WeixinPayResponse() {

    override fun isBizOk(): Boolean {
        return "SUCCESS" == resultCode && "SUCCESS" == tradeState
    }
}
