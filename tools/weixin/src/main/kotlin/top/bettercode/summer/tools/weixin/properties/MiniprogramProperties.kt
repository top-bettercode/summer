package top.bettercode.summer.tools.weixin.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.weixin.support.miniprogram.entity.MiniprogramState

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.wechat.mini")
open class MiniprogramProperties : WexinProperties(), IMiniprogramProperties {
    /**
     *  跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
    override var miniprogramState: String = MiniprogramState.formal
}
