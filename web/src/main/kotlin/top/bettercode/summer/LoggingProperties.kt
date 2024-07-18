package top.bettercode.summer

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.log.feishu.FeishuProperties
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
open class FeishuLoggingProperties: FeishuProperties()