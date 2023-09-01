package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
open class PayResponse(

        /**
         * 小程序ID；必填；微信分配的小程序ID；示例：wx8888888888888888
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：1900000109
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 设备号；非必填；微信支付分配的终端设备号；示例：013467007045764
         */
        @field:JsonProperty("device_info")
        var deviceInfo: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 签名；必填；签名，详见签名算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 签名类型；非必填；签名类型，目前支持HMAC-SHA256和MD5，默认为MD5；示例：HMAC-SHA256
         */
        @field:JsonProperty("sign_type")
        var signType: String? = null,
        /**
         * 业务结果；必填；SUCCESS/FAIL；示例：SUCCESS
         */
        @field:JsonProperty("result_code")
        var resultCode: String? = null,
        /**
         * 错误代码；非必填；错误返回的信息描述；示例：SYSTEMERROR
         */
        @field:JsonProperty("err_code")
        var errCode: String? = null,
        /**
         * 错误代码描述；非必填；错误返回的信息描述；示例：系统错误
         */
        @field:JsonProperty("err_code_des")
        var errCodeDes: String? = null,
        /**
         * 用户标识；必填；用户在商户appid下的唯一标识；示例：wxd930ea5d5a258f4f
         */
        @field:JsonProperty("openid")
        var openid: String? = null,
        /**
         * 是否关注公众账号；必填；已废弃，默认统一返回N；示例：N
         */
        @field:JsonProperty("is_subscribe")
        var isSubscribe: String? = null,
        /**
         * 交易类型；必填；JSAPI、NATIVE、APP；示例：JSAPI
         */
        @field:JsonProperty("trade_type")
        var tradeType: String? = null,
        /**
         * 付款银行；必填；银行类型，采用字符串类型的银行标识，银行类型见银行列表；示例：CMC
         */
        @field:JsonProperty("bank_type")
        var bankType: String? = null,
        /**
         * 订单金额；必填；订单总金额，单位为分；示例：100
         */
        @field:JsonProperty("total_fee")
        var totalFee: Int? = null,
        /**
         * 应结订单金额；非必填；应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额；示例：100
         */
        @field:JsonProperty("settlement_total_fee")
        var settlementTotalFee: Int? = null,
        /**
         * 货币种类；非必填；货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型；示例：CNY
         */
        @field:JsonProperty("fee_type")
        var feeType: String? = null,
        /**
         * 现金支付金额；必填；现金支付金额订单现金支付金额，详见支付金额；示例：100
         */
        @field:JsonProperty("cash_fee")
        var cashFee: Int? = null,
        /**
         * 现金支付货币类型；非必填；货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型；示例：CNY
         */
        @field:JsonProperty("cash_fee_type")
        var cashFeeType: String? = null,
        /**
         * 总代金券金额；非必填；代金券金额<=订单金额，订单金额-代金券金额=现金支付金额，详见支付金额；示例：10
         */
        @field:JsonProperty("coupon_fee")
        var couponFee: Int? = null,
        /**
         * 代金券使用数量；非必填；代金券使用数量；示例：1
         */
        @field:JsonProperty("coupon_count")
        var couponCount: Int? = null,
        /**
         * 微信支付订单号；必填；微信支付订单号；示例：1217752501201407033233368018
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
        /**
         * 商户订单号；必填；商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号；示例：1212321211201407033568112322
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 商家数据包；非必填；商家数据包，原样返回；示例：123456
         */
        @field:JsonProperty("attach")
        var attach: String? = null,
        /**
         * 支付完成时间；必填；支付完成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则；示例：20141030133525
         */
        @field:JsonProperty("time_end")
        var timeEnd: String? = null,
        /**
         * 其他
         */
        @get:JsonAnyGetter
        @field:JsonAnySetter
        var other: Map<String, Any?>? = null
        /**
         * 代金券类型；非必填；CASH--充值代金券 NO_CASH---非充值代金券 并且订单使用了免充值券后有返回（取值：CASH、NO_CASH）。$n为下标,从0开始编号，举例：coupon_type_0 注意：只有下单时订单使用了优惠，回调通知才会返回券信息。 下列情况可能导致订单不可以享受优惠：可能情况；示例：CASH
         */
        //@field:JsonProperty("coupon_type_$n")
        //var couponType$n: String? = null,
        /**
         * 代金券ID；非必填；代金券ID,$n为下标，从0开始编号 注意：只有下单时订单使用了优惠，回调通知才会返回券信息。 下列情况可能导致订单不可以享受优惠：可能情况；示例：10000
         */
        //@field:JsonProperty("coupon_id_$n")
        //var couponId$n: String? = null,
        /**
         * 单个代金券支付金额；非必填；单个代金券支付金额,$n为下标，从0开始编号；示例：100
         */
        //@field:JsonProperty("coupon_fee_$n")
        //var couponFee$n: Int? = null,
) : WeixinPayResponse() {

    override fun isBizOk(): Boolean {
        return "SUCCESS" == resultCode
    }
}
