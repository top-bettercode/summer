package top.bettercode.summer.tools.feishu

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties

/**
 * 飞书平台 配置
 *
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.feishu")
open class FeishuProperties : ClientProperties("飞书平台") {

    val api = "https://open.feishu.cn/open-apis"

    var appId: String = ""

    var appSecret: String = ""

    /**
     * 默认不在飞书的工号
     */
    var defaultNotInJobNo: Array<String> = arrayOf()
}