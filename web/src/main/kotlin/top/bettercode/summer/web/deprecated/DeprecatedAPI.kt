package top.bettercode.summer.web.deprecated

import org.springframework.core.annotation.AliasFor
import java.lang.annotation.Inherited

/**
 * 已弃用的接口
 *
 * @author Peter Wu
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
annotation class DeprecatedAPI(
        /**
         * @return 提示消息, 默认“该功能已弃用”
         */
        @get:AliasFor("message") val value: String = "deprecated.api",
        /**
         * @return 提示消息, 默认“该功能已弃用”
         */
        @get:AliasFor("value") val message: String = "deprecated.api")
