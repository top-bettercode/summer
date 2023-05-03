package top.bettercode.summer.data.jpa

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SoftDelete(
        /**
         * @return 默认逻辑未删除值, 默认获取全局配置
         */
        val falseValue: String = "",
        /**
         * @return 默认逻辑删除值, 默认获取全局配置
         */
        val trueValue: String = ""
) 