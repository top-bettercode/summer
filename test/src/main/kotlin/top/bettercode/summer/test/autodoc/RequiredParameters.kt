package top.bettercode.summer.test.autodoc

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
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

    fun calculate(handler: HandlerMethod?): ParamInfo {
        val requiredParameters: MutableSet<String> = mutableSetOf()
        val defaultValueParams: MutableMap<String, String> = mutableMapOf()
        var existNoAnnoDefaultPageParam = false

        val requestMapping = handler?.getMethodAnnotation(RequestMapping::class.java)
        val requiredParams = requestMapping?.params?.filter { !it.startsWith("!") }
        if (!requiredParams.isNullOrEmpty()) {
            requiredParameters.addAll(requiredParams)
        }
        handler?.methodParameters?.forEach {
            existNoAnnoDefaultPageParam =
                it.parameterType.name == "org.springframework.data.domain.Pageable" && !it.hasParameterAnnotation(
                    org.springframework.data.web.PageableDefault::class.java
                )
            val requestParam = it.getParameterAnnotation(RequestParam::class.java)
            if (requestParam != null) {
                defaultValueParams[it.parameterName!!] = requestParam.defaultValue
            }
            if (it.parameterName != null && (it.hasParameterAnnotation(NotNull::class.java) || it.hasParameterAnnotation(
                    NotBlank::class.java
                ) || it.hasParameterAnnotation(NotEmpty::class.java) || requestParam?.required == true)
            ) {
                requiredParameters.add(it.parameterName!!)
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
        return ParamInfo(requiredParameters, defaultValueParams, existNoAnnoDefaultPageParam)
    }


    private fun addRequires(
        clazz: Class<*>,
        requires: MutableSet<String>,
        groups: Array<out KClass<out Any>>,
        prefix: String = ""
    ) {
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