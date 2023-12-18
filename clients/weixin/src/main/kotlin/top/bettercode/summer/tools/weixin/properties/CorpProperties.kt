package top.bettercode.summer.tools.weixin.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@Suppress("ConfigurationProperties")
@ConfigurationProperties(prefix = "summer.wechat.corp")
open class CorpProperties : WeixinProperties(), ICorpProperties {
    /**
     * 项目部署的URL地址
     */
    override var appBaseUrl: String? = null

    /**
     * 微信前端基础地址
     */
    override var wechatBaseUrl: String? = null

    /**
     * 微信前端授权页面地址
     */
    override var wechatWebOauthUrl = ""
    override val basicAccessTokenUrl: String
        //--------------------------------------------
        get() = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid={0}&corpsecret={1}"
}
