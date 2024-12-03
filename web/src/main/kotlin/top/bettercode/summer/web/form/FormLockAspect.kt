package top.bettercode.summer.web.form

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.lang.util.DirectFieldAccessFallbackBeanWrapper
import java.time.Duration


@Aspect
class FormLockAspect(private val formkeyService: FormkeyService) {

    private val log: Logger = LoggerFactory.getLogger(FormLockAspect::class.java)

    @Around("@annotation(top.bettercode.summer.web.form.FormLock)")
    @Throws(Throwable::class)
    fun logProceed(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        try {
            val method = signature.method
            val annotation = method.getAnnotation(FormLock::class.java)
            val paramKey = if (annotation.paramName.isNotBlank()) {
                val paramNames = annotation.paramName.split(".")
                val indexOf = signature.parameterNames.indexOf(paramNames[0])
                if (indexOf >= 0) {
                    val arg = joinPoint.args[indexOf]
                    getNestedProperty(arg, paramNames.subList(1, paramNames.size))
                } else {
                    null
                }
            } else {
                null
            }

            return formkeyService.runIfAbsentOrElseThrow(
                formkey = annotation.value + (paramKey ?: ""),
                ttl = if (annotation.ttl.isNotBlank()) Duration.parse(annotation.ttl) else null,
                expMsg = annotation.message,
                waitTime = if (annotation.waitTime.isNotBlank()) Duration.parse(annotation.waitTime) else null,
            ) {
                joinPoint.proceed()
            }
        } finally {

        }
    }

    fun getNestedProperty(obj: Any?, properties: List<String>): Any? {
        try {
            if (obj == null || properties.isEmpty()) {
                return null
            }
            var currentObject = obj
            for (property in properties) {
                if (currentObject == null) {
                    return null
                }
                currentObject =
                    DirectFieldAccessFallbackBeanWrapper(currentObject).getPropertyValue(property)
            }
            return currentObject
        } catch (e: Exception) {
            log.warn("获取属性值失败", e)
            return null
        }
    }
}
