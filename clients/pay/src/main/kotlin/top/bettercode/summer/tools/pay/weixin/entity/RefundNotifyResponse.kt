package top.bettercode.summer.tools.pay.weixin.entity

import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class RefundNotifyResponse(

        /**
         * 应用ID；必填；微信开放平台审核通过的应用APPID；示例：wx8888888888888888
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 退款的商户号；必填；微信支付分配的商户号；示例：1900000109
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位。推荐随机数生成算法；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 加密信息；必填；加密信息请用商户密钥进行解密，详见解密方式；示例：T87GAHG17TGAHG1TGHAHAHA1Y1CIOA9UGJH1GAHV871HAGAGQYQQPOOJMXNBCXBVNMNMAJAA
         */
        @field:JsonProperty("req_info")
        var reqInfo: String? = null,
) : WeixinPayResponse() {

    override fun isBizOk(): Boolean {
        return true
    }
}
