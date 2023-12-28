package top.bettercode.summer.data.jpa.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Peter Wu
 */
@ConfigurationProperties("summer.data.jpa")
open class JpaExtProperties {
    /**
     * 超时警报时间秒数
     */
    var timeoutAlarmSeconds = 2

    /**
     * 逻辑删除
     */
    var logicalDelete = LogicalDelete()

    class LogicalDelete {
        /**
         * 默认逻辑删除值.
         */
        var trueValue: Any = 1

        /**
         * 默认逻辑未删除值.
         */
        var falseValue: Any = 0
    }
}
