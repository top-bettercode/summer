package top.bettercode.summer.tools.weixin.support.offiaccount.entity

/**
 * 事件类型,subscribe(订阅)、unsubscribe(取消订阅)
 *
 * subscribe:用户未关注时，进行关注后的事件推送,
 * SCAN:扫描带参数二维码事件
 * LOCATION:上报地理位置事件
 * CLICK:自定义菜单事件
 * VIEW:点击菜单跳转链接时的事件推送
 *
 * @author Peter Wu
 */
object EventType {
    /**
     * 订阅事件
     */
    const val SUBSCRIBE = "subscribe"

    /**
     * 取消订阅事件
     */
    const val UNSUBSCRIBE = "unsubscribe"

    /**
     * 扫描带参数二维码事件
     */
    const val SCAN = "SCAN"

    /**
     * 上报地理位置事件
     */
    const val LOCATION = "LOCATION"

    /**
     * 自定义菜单事件
     */
    const val CLICK = "CLICK"

    /**
     * 点击菜单跳转链接时的事件推送
     */
    const val VIEW = "VIEW"
}