package top.bettercode.summer.data.jpa.config

/**
 * @author Peter Wu
 */
class JpaExtProperties {
    var softDelete = SoftDelete()

    class SoftDelete {
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
