package cn.bestwu.autodoc.gen

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ValueConstants
import org.springframework.web.method.HandlerMethod
import java.lang.reflect.ParameterizedType
import javax.validation.Validation
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.groups.Default
import kotlin.reflect.KClass

/**
 *
 * @author Peter Wu
 */
object RequiredParameters {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    fun calculateHeaders(handler: HandlerMethod?): Map<String, String> {
        val requiredHeaders = mutableMapOf<String, String>()
        handler?.methodParameters?.forEach {
            val requestHeader = it.getParameterAnnotation(RequestHeader::class.java)
            if (it.hasParameterAnnotation(RequestHeader::class.java) && (it.hasParameterAnnotation(
                    NotNull::class.java
                ) || it.hasParameterAnnotation(NotBlank::class.java) || it.hasParameterAnnotation(
                    NotEmpty::class.java
                ) || requestHeader?.required == true)
            ) {
                if (it.parameterName != null)
                    requiredHeaders[it.parameterName!!] = requestHeader?.defaultValue
                        ?: ValueConstants.DEFAULT_NONE
            }
        }
        return requiredHeaders
    }

    fun calculate(handler: HandlerMethod?): Map<String, String> {
        val requiredParameters = mutableMapOf<String, String>()
        handler?.methodParameters?.forEach {
            val requestParam = it.getParameterAnnotation(RequestParam::class.java)
            if (it.hasParameterAnnotation(NotNull::class.java) || it.hasParameterAnnotation(NotBlank::class.java) || it.hasParameterAnnotation(
                    NotEmpty::class.java
                ) || requestParam?.required == true
            ) {
                if (it.parameterName != null) {
                    requiredParameters[it.parameterName!!] = requestParam?.defaultValue
                        ?: ValueConstants.DEFAULT_NONE
                }
            }

            var clazz = it.parameterType
            if (clazz.isArray) {
                clazz = clazz.componentType
            } else if (Collection::class.java.isAssignableFrom(clazz)) {
                clazz =
                    (it.genericParameterType as ParameterizedType).actualTypeArguments[0] as Class<*>
            }
            if (clazz.classLoader != null) {
                val validatedAnn = it.getParameterAnnotation(Validated::class.java)
                var hints = validatedAnn?.value ?: arrayOf(Default::class)
                if (hints.isEmpty()) {
                    hints = arrayOf(Default::class)
                }
                addRequires(clazz, requiredParameters, hints)
            }
        }
        return requiredParameters
    }


    private fun addRequires(
        clazz: Class<*>,
        requires: MutableMap<String, String>,
        groups: Array<out KClass<out Any>>,
        prefix: String = ""
    ) {
        val constraintsForClass = validator.getConstraintsForClass(clazz)
        constraintsForClass.constrainedProperties.forEach { pd ->
            pd.constraintDescriptors.forEach { cd ->
                if (groups.any { cd.groups.contains(it.java) }) {
                    if (cd.annotation is NotNull || cd.annotation is NotBlank || cd.annotation is NotEmpty) {
                        requires[prefix + pd.propertyName] = ValueConstants.DEFAULT_NONE
                    }
                }
            }

            if ((pd.elementClass.isArray || Collection::class.java.isAssignableFrom(pd.elementClass))) {
                pd.constrainedContainerElementTypes.forEach {
                    if (it.elementClass?.classLoader != null) {
                        addRequires(
                            it.elementClass,
                            requires,
                            groups,
                            "${prefix + pd.propertyName}."
                        )
                    }
                }
            }
        }
    }
}