package top.bettercode.summer.tools.pay.weixin.entity

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import top.bettercode.summer.tools.lang.util.RandomUtil

/**
 * @author Peter Wu
 */
@JacksonXmlRootElement(localName = "xml")
data class AppWCPayRequest @JvmOverloads constructor(

        /**
         * 应用ID；必填；微信开放平台审核通过的应用APPID（请登录open.weixin.qq.com查看，注意与公众号的APPID不同）；示例：wx8888888888888888
         */
        @field:JsonProperty("appid")
        var appid: String,
        /**
         * 商户号；必填；微信支付分配的商户号；示例：1900000109
         */
        @field:JsonProperty("partnerid")
        var partnerid: String,
        /**
         * 预支付交易会话ID；必填；微信返回的支付交易会话ID；示例：WX1217752501201407033233368018
         */
        @field:JsonProperty("prepayid")
        var prepayid: String? = null,
        /**
         * 签名；必填；签名，详见签名生成算法注意：签名方式一定要与统一下单接口使用的一致；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("sign")
        var sign: String? = null,
        /**
         * 扩展字段；必填；暂填写固定值Sign=WXPay；示例：Sign=WXPay
         */
        @field:JsonProperty("package")
        var `package`: String? = "Sign=WXPay",
        /**
         * 随机字符串；必填；随机字符串，不长于32位。推荐随机数生成算法；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("noncestr")
        var noncestr: String = RandomUtil.nextString2(32),
        /**
         * 时间戳；必填；时间戳，请见接口规则-参数规定；示例：1412000000
         */
        @field:JsonProperty("timestamp")
        var timestamp: String = (System.currentTimeMillis() / 1000).toString(),
        /**
         * 其他
         */
        @get:JsonAnyGetter
        @field:JsonAnySetter
        var other: MutableMap<String, Any?> = mutableMapOf()
) {

    @JsonIgnore
    fun put(key: String, value: Any?) {
        other[key] = value
    }

    @JsonIgnore
    fun get(key: String): Any? {
        return other[key]
    }
}

