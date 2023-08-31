
package top.bettercode.summer.tools.pay.weixin.entity
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse

/**
 * @author Peter Wu
 */
data class PayNotifyResponse(

        /**
         * 小程序ID，微信分配的小程序ID
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号，微信支付分配的商户号
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 设备号，微信支付分配的终端设备号，
         */
        @field:JsonProperty("device_info")
        var deviceInfo: String? = null,
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
         * 签名类型，签名类型，目前支持HMAC-SHA256和MD5，默认为MD5
         */
        @field:JsonProperty("sign_type")
        var signType: String? = null,
        /**
         * 业务结果，SUCCESS/FAIL
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码，错误返回的信息描述
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述，错误返回的信息描述
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
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
         * 交易类型，JSAPI、NATIVE、APP
         */
        @field:JsonProperty("trade_type")
        var tradeType: String? = null,
        /**
         * 付款银行，银行类型，采用字符串类型的银行标识，银行类型见银行列表
         */
        @field:JsonProperty("bank_type")
        var bankType: String? = null,
        /**
         * 订单金额，订单总金额，单位为分
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 应结订单金额，应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。
         */
        @field:JsonProperty("settlement_total_fee")
        var settlementTotalFee: Int? = null,
        /**
         * 货币种类，货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
         */
        @field:JsonProperty("fee_type")
        var feeType: String? = null,
        /**
         * 现金支付金额，现金支付金额订单现金支付金额，详见支付金额
         */
        @field:JsonProperty("cash_fee")
        var cashFee: Int? = null,
        /**
         * 现金支付货币类型，货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
         */
        @field:JsonProperty("cash_fee_type")
        var cashFeeType: String? = null,
        /**
         * 总代金券金额，代金券金额<=订单金额，订单金额-代金券金额=现金支付金额，详见支付金额
         */
        @field:JsonProperty("coupon_fee")
        var couponFee: Int? = null,
        /**
         * 代金券使用数量，代金券使用数量
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
         * 商家数据包，商家数据包，原样返回
         */
        @field:JsonProperty("attach")
        var attach: String? = null,
        /**
         * 支付完成时间，支付完成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
         */
        @field:JsonProperty("time_end")
        var timeEnd: String? = null,
        /**
         * 其他
         */
        @field:JsonAnyGetter
        @field:JsonAnySetter
        var other: Map<String, Any?>? = null
        /**
         * 代金券类型，CASH--充值代金券 NO_CASH---非充值代金券 并且订单使用了免充值券后有返回（取值：        CASH、NO_CASH）。$n为下标,从0开始编号，举例：coupon_type_0 注意：只有下单时订单使        用了优惠，回调通知才会返回券信息。 下列情况可能导致订单不可以享受优惠：可能情况。
         */
        //@field:JsonProperty("coupon_type_$n")
        //var couponType$n: String? = null,
        /**
         * 代金券ID，代金券ID,$n为下标，从0开始编号 注意：只有下单时订单使用了优惠，回调通知才会返回券信        息。 下列情况可能导致订单不可以享受优惠：可能情况。
         */
        //@field:JsonProperty("coupon_id_$n")
        //var couponId$n: String? = null,
        /**
         * 单个代金券支付金额，单个代金券支付金额,$n为下标，从0开始编号
         */
        //@field:JsonProperty("coupon_fee_$n")
        //var couponFee$n: Int? = null,
) : WeixinPayResponse() {
        /**
         * 业务结果
         */
        override fun isBizOk(): Boolean {
                return resultCode == "SUCCESS"
        }
}
