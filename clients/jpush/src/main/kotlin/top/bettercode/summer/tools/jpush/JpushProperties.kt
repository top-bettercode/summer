package top.bettercode.summer.tools.jpush

import org.springframework.boot.context.properties.ConfigurationProperties
import top.bettercode.summer.tools.lang.client.ClientProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.jpush")
open class JpushProperties : ClientProperties("极光推送平台") {
    /**
     * 接口地址
     */
    var url = "https://api.jpush.cn/v3"

    /**
     * appKey
     */
    var appKey: String = ""

    /**
     * masterSecret
     */
    var masterSecret: String = ""

    /**
     * APNs 是否生产环境 该字段仅对 iOS 的 Notification 有效，如果不指定则为推送生产环境。注意：JPush 服务端 SDK 默认设置为推送 “开发环境”。
     * true：表示推送生产环境。 false：表示推送开发环境。
     */
    var apnsProduction = false

    /**
     * 离线消息保留时长 (秒) 推送当前用户不在线时，为该用户保留多长时间的离线消息，以便其上线时再次推送。 默认 86400 （1 天），普通用户最长 3 天， VIP 用户最长 10
     * 天。设置为 0 表示不保留离线消息，只有推送当前在线的用户可以收到。 该字段对 iOS 的 Notification 消息无效。
     */
    var timeToLive = 60 * 60 * 24 * 3L
}
