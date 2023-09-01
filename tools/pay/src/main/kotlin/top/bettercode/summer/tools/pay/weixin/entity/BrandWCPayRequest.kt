package top.bettercode.summer.tools.pay.weixin.entity


import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Peter Wu
 */
data class BrandWCPayRequest @JvmOverloads constructor(

        /**
         * 公众号id；必填；appId为当前服务商号绑定的appid；示例：wx8888888888888888
         */
        @field:JsonProperty("appId")
        var appId: String? = null,
        /**
         * 时间戳；必填；当前的时间，其他详见时间戳规则；示例：1414561699
         */
        @field:JsonProperty("timeStamp")
        var timeStamp: String? = null,
        /**
         * 随机字符串；必填；随机字符串，不长于32位。推荐随机数生成算法；示例：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
         */
        @field:JsonProperty("nonceStr")
        var nonceStr: String? = null,
        /**
         * 订单详情扩展字符串；必填；统一下单接口返回的prepay_id参数值，提交格式如：prepay_id=***；示例：prepay_id=123456789
         */
        @field:JsonProperty("package")
        var `package`: String? = null,
        /**
         * 签名方式；必填；签名类型，默认为MD5，支持HMAC-SHA256和MD5。注意此处需与统一下单的签名类型一致；示例：MD5
         */
        @field:JsonProperty("signType")
        var signType: String? = null,
        /**
         * 签名；必填；签名，详见签名生成算法；示例：C380BEC2BFD727A4B6845133519F3AD6
         */
        @field:JsonProperty("paySign")
        var paySign: String? = null,
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
