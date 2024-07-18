package top.bettercode.summer

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import top.bettercode.summer.tools.lang.log.feishu.FeishuProperties
import top.bettercode.summer.tools.lang.log.feishu.FeishuWebHook
import top.bettercode.summer.tools.lang.log.slack.SlackProperties

/**
 *
 * @author Peter Wu
 */
@Suppress("ConfigurationProperties")
@ConfigurationProperties("summer.logging.slack")
open class SlackLoggingProperties: SlackProperties()

@Suppress("ConfigurationProperties")
@ConfigurationProperties("summer.logging.feishu")
open class FeishuLoggingProperties: FeishuProperties(){

    @NestedConfigurationProperty
    override var chatHook: FeishuWebHook? = null
    @NestedConfigurationProperty
    override var timeoutChatHook: FeishuWebHook? =null
}