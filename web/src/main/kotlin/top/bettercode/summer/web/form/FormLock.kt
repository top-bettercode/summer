package top.bettercode.summer.web.form

import java.lang.annotation.Inherited

/**
 * @author Peter Wu
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
annotation class FormLock(
    /**
     * @return form key名称前缀
     */
    val value: String,
    /**
     * @return form key 参数名称
     */
    val paramName: String = "",
    /**
     * @return 等待时间，示例：PT30S
     */
    val waitTime: String = "",
    /**
     * @return 有效期限，示例：PT30M
     */
    val ttl: String = "PT1S",
    /**
     * @return 提示信息
     */
    val message: String = FormDuplicateCheckInterceptor.DEFAULT_MESSAGE,
)

