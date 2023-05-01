package top.bettercode.summer.web.validator

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

/**
 * 中国（CN）大陆地区身份证号码验证
 *
 * @author P
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [IDCardValidator::class])
@MustBeDocumented
annotation class IDCard( // 定义错误消息
        val message: String = "错误的身份证号码",  // 定义所在的组
        val groups: Array<KClass<*>> = [],  // 定义级别条件的严重级别
        val payload: Array<KClass<out Payload>> = [])