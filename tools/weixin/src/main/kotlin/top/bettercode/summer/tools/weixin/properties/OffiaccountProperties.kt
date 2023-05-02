package top.bettercode.summer.tools.weixin.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.wechat")
class OffiaccountProperties : WexinProperties(), IOffiaccountProperties {
    //--------------------------------------------
    override var userUnionid = false
        private set

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
    fun setUserUnionid(userUnionid: Boolean): OffiaccountProperties {
        this.userUnionid = userUnionid
        return this
    }
}
