package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonProperty
import top.bettercode.summer.tools.pay.weixin.WeixinPayResponse

/**
 * @author Peter Wu
 */
data class RefundNotifyResponse(

        /**
         * 应用ID，微信开放平台审核通过的应用APPID
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * 退款的商户号，微信支付分配的商户号
         */
        @field:JsonProperty("mch_id")
        var mchId: String? = null,
        /**
         * 随机字符串，随机字符串，不长于32位。推荐随机数生成算法
         */
        @field:JsonProperty("nonce_str")
        var nonceStr: String? = null,
        /**
         * 加密信息，加密信息请用商户密钥进行解密，详见解密方式
         */
        @field:JsonProperty("req_info")
        var reqInfo: String? = null,
) : WeixinPayResponse() {
    /**
     * 业务结果
     */
    override fun isBizOk(): Boolean {
        return true
    }
}

