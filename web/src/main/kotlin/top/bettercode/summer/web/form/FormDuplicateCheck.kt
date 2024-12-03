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
annotation class FormDuplicateCheck(
    /**
     * @return form key过期时间，单位秒
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

