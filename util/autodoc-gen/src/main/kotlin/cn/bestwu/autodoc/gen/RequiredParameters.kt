package cn.bestwu.autodoc.gen

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.method.HandlerMethod
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

    fun calculateHeaders(handler: HandlerMethod?): MutableSet<String> {
        val requiredHeaders = mutableSetOf<String>()
        handler?.methodParameters?.forEach {
            if (it.hasParameterAnnotation(RequestHeader::class.java) && (it.hasParameterAnnotation(NotNull::class.java) || it.hasParameterAnnotation(NotBlank::class.java) || it.hasParameterAnnotation(NotEmpty::class.java) || it.getParameterAnnotation(RequestHeader::class.java)?.required == true)) {
                if (it.parameterName != null)
                    requiredHeaders.add(it.parameterName!!)
            }
        }
        return requiredHeaders
    }

    fun calculate(handler: HandlerMethod?): MutableSet<String> {
        val requiredParameters = mutableSetOf<String>()
        handler?.methodParameters?.forEach {
            if (it.hasParameterAnnotation(NotNull::class.java) || it.hasParameterAnnotation(NotBlank::class.java) || it.hasParameterAnnotation(NotEmpty::class.java) || it.getParameterAnnotation(RequestParam::class.java)?.required == true) {
                if (it.parameterName != null)
                    requiredParameters.add(it.parameterName!!)
            }
            if (it.parameterType.classLoader != null) {
                val validatedAnn = it.getParameterAnnotation(Validated::class.java)
                var hints = validatedAnn?.value ?: arrayOf(Default::class)
                if (hints.isEmpty()) {
                    hints = arrayOf(Default::class)
                }
                addRequires(it.parameterType, requiredParameters, hints)
            }
        }
        return requiredParameters
    }


    private fun addRequires(clazz: Class<*>, requires: MutableSet<String>, groups: Array<out KClass<out Any>>, prefix: String = "") {
        val constraintsForClass = validator.getConstraintsForClass(clazz)
        constraintsForClass.constrainedProperties.forEach { pd ->
            pd.constraintDescriptors.forEach { cd ->
                if (groups.any { cd.groups.contains(it.java) }) {
                    if (cd.annotation is NotNull || cd.annotation is NotBlank || cd.annotation is NotEmpty) {
                        requires.add(prefix + pd.propertyName)
                    }
                }
            }

            if ((pd.elementClass.isArray || Collection::class.java.isAssignableFrom(pd.elementClass))) {
                pd.constrainedContainerElementTypes.forEach {
                    if (it.elementClass.classLoader != null) {
                        addRequires(it.elementClass, requires, groups, "${prefix + pd.propertyName}.")
                    }
                }
            }
        }
    }
}