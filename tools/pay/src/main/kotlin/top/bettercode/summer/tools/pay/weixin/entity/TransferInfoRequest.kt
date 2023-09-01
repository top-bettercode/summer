package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.util.RandomUtil

/**
 * @author Peter Wu
 */
data class TransferInfoRequest @JvmOverloads constructor(

        /**
         * 商户订单号；必填；商户调用付款API时使用的商户订单号；示例：10000098201411111234567890
         */
        @field:JsonProperty("partner_trade_no")
        var partnerTradeNo: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = RandomUtil.nextString2(32),
        /**
         * 签名；必填；签名，详见签名算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：10000098
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * Appid；必填；商户号的appid；示例：wxe062425f740d30d8
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
)
