package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.util.RandomUtil

/**
 * @author Peter Wu
 */
data class OrderQueryRequest(

        /**
         * 商户订单号；非必填；商户系统内部的订单号，当没提供transaction_id时需要传这个；示例：20150806125346
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 微信订单号；非必填；微信的订单号，优先使用；示例：1009660380201506130728806387
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,
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
         * 随机字符串；必填；随机字符串，不长于32位。推荐随机数生成算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = RandomUtil.nextString2(32),
        /**
         * 签名；必填；签名，详见签名生成算法；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
)
