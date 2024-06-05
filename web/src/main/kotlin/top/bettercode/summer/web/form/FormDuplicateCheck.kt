package top.bettercode.summer.web.form

import java.lang.annotation.Inherited

/**
 * @author Peter Wu
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
annotation class FormDuplicateCheck(
    /**
     * 使用 [RedisFormkeyService] 时，支持指定过期时间，单位秒
     *
     * @return form key有效时间
     */
    val expireSeconds: Long = -1,
    /**
     * @return 提示信息
     */
    val message: String = FormDuplicateCheckInterceptor.DEFAULT_MESSAGE,
    /**
     *  @return 生成key时过滤header
     */
    val ignoreHeaders: Array<String> = [],
    /**
     * @return 生成key时过滤参数
     */
    val ignoreParams: Array<String> = [],
)

