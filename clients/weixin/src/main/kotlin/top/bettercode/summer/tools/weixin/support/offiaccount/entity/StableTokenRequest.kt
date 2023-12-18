package top.bettercode.summer.tools.weixin.support.offiaccount.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * @author Peter Wu
 */
@JacksonXmlRootElement(localName = "xml")
data class StableTokenRequest @JvmOverloads constructor(
        /**
         * appid；必填；账号唯一凭证，即 AppID，可在「微信公众平台 - 设置 - 开发设置」页中获得。（需要已经成为开发者，且账号没有异常状态）；示例：AppID，可在「微信公众平台
         */
        @field:JsonProperty("appid")
        var appid: String? = null,
        /**
         * secret；必填；账号唯一凭证密钥，即 AppSecret，获取方式同 appid；示例：AppSecret，获取方式同
         */
        @field:JsonProperty("secret")
        var secret: String? = null,
        /**
         * force_refresh；非必填；默认使用 false。1. force_refresh = false 时为普通调用模式，access_token 有效期内重复调用该接口不会更新 access_token；2. 当force_refresh = true 时为强制刷新模式，会导致上次获取的 access_token 失效，并返回新的 access_token；示例：boolean
         */
        @field:JsonProperty("force_refresh")
        var forceRefresh: Boolean? = null,
        /**
         * grant_type；必填；填写 client_credential；示例：client_credential
         */
        @field:JsonProperty("grant_type")
        var grantType: String? = "client_credential"
)
