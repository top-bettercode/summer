import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.lang.util.RandomUtil

/**
 * @author Peter Wu
 */
data class OrderQueryRequest(
        /**
         * 商户订单号，商户系统内部的订单号，当没提供transaction_id时需要传这个。
         */
        @field:JsonProperty("out_trade_no")
        var outTradeNo: String? = null,
        /**
         * 微信订单号，微信的订单号，优先使用
         */
        @field:JsonProperty("transaction_id")
        var transactionId: String? = null,

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
        var nonceStr: String? = RandomUtil.nextString2(32),
        /**
         * 签名，签名，详见签名生成算法
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
)
