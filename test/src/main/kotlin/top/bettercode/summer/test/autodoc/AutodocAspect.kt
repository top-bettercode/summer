package top.bettercode.summer.test.autodoc

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanWrapperImpl
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.io.InputStreamSource
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.lang.util.FileUtil
import top.bettercode.summer.web.PagedResources
import java.beans.Introspector
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import javax.persistence.EntityManagerFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Aspect
class AutodocAspect(
    private val entityManagerFactory: List<EntityManagerFactory>,
    private val applicationContext: GenericApplicationContext
) {

    private val log: Logger = LoggerFactory.getLogger(AutodocAspect::class.java)
    private val entityTypes: List<String> by lazy {
        entityManagerFactory.flatMap { em -> em.metamodel.entities.map { it.javaType.simpleName } }
    }

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.PutMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    fun autodoc(joinPoint: ProceedingJoinPoint): Any? {
        val result = joinPoint.proceed()
        if (Autodoc.enable) {
            try {
                val signature = joinPoint.signature as MethodSignature
                val method = signature.method
                if (!ErrorController::class.java.isAssignableFrom(method.declaringClass)) {
                    extMethodParamComments(method)

                    val args = joinPoint.args
                    method.parameterTypes.forEachIndexed { index, clazz ->
                        val arg = args[index]
                        val type = if (arg == null) clazz else arg::class.java
                        extDocFieldInfo(type, arg)
                    }
                    val resultType = if (result == null) method.returnType else result::class.java
                    extDocFieldInfo(resultType, result)
                }
            } catch (e: Exception) {
                log.error("解析参数实体类型失败", e)
            }
        }
        return result
    }


    fun isEntity(type: String): Boolean {
        return entityTypes.contains(type)
    }

    private fun extMethodParamComments(method: Method) {
        val className = method.declaringClass.name
        val pathname = "src/main/java/${className.replace(".", "/")}.java"
        var file = File(pathname)
        if (!file.exists()) {
            rootProject.walkTopDown().find {
                it.isFile && it.absolutePath.endsWith(pathname)
            }?.let {
                file = it
            }
        }
        if (file.exists()) {
            val methodName = method.name
            val parameters = method.parameters.associate {
                it.name to it.type
                    .simpleName
            }
            val cu = StaticJavaParser.parse(file)
            val targetMethodOpt = cu.findAll(MethodDeclaration::class.java)
                .firstOrNull { m ->
                    m.nameAsString == methodName && m.parameters.size == method.parameterCount && m.parameters.map { it.type } == method.parameterTypes.map {
                        StaticJavaParser.parseType(
                            it.simpleName
                        )
                    }
                } ?: return

            val fields = linkedSetOf<Field>()

            targetMethodOpt.javadoc?.ifPresent { javadoc ->
                javadoc.blockTags
                    .filter { it.tagName == "param" }
                    .forEach { paramTag ->
                        val paramName = paramTag.name.orElse("").trim()
                        val paramDescription = paramTag.content.toText().trim()

                        if (paramName.isNotBlank() && paramDescription.isNotBlank()) {
                            fields.add(
                                Field(
                                    name = paramName,
                                    type = parameters[paramName]!!,
                                    description = paramDescription
                                )
                            )
                        }
                    }
            }
            if (fields.isNotEmpty()) {
                if (log.isDebugEnabled) {
                    log.debug("自动识别方法参数：{}", fields)
                }
                Autodoc.fields["ARGS"] = fields
            }
        }
    }


    private fun extDocFieldInfo(type: Class<*>, any: Any?) {
        if (!Autodoc.extedTypes.contains(type) && valided(type)) {
            extFields(type)
            getClassHierarchy(type).forEach { cls ->
                extFields(cls)
            }
            getClassGetters(type, any)?.forEach { (cls, value) ->
                extFields(cls)
                extDocFieldInfo(cls, value)
            }
            Autodoc.extedTypes.add(type)
        }
    }

    private fun extFields(cls: Class<*>) {
        val simpleName = cls.simpleName
        if (!Autodoc.fields.containsKey(simpleName)) {
            fields(cls)?.let { fields ->
                if (fields.isNotEmpty()) {
                    Autodoc.fields[simpleName] = fields
                    if (!isEntity(simpleName)) {
                        if (log.isDebugEnabled) {
                            log.debug("自动识别参数：{}", fields)
                        }
                    }
                }
            }
        }
    }

    private fun fields(type: Class<*>): LinkedHashSet<Field>? {
        val pathname = "src/main/java/${type.name.replace(".", "/")}.java"
        var file = File(pathname)
        if (!file.exists()) {
            rootProject.walkTopDown().find {
                it.isFile && it.absolutePath.endsWith(pathname)
            }?.let {
                file = it
            }
        }
        if (file.exists()) {
            val fields = linkedSetOf<Field>()
            val cu = StaticJavaParser.parse(file)
            cu.findAll(FieldDeclaration::class.java).forEach { field ->
                field.variables.forEach { variable ->
                    val fieldName = variable.nameAsString
                    val comment =
                        field.comment.map {
                            it.content.trim().trimStart('*').trim().substringBefore(" 默认值：")
                        }.orElse("")
                    if (comment.isNotBlank()) {
                        fields.add(
                            Field(
                                name = fieldName,
                                type = variable.typeAsString.substringBefore("<"),
                                description = comment
                            )
                        )
                    }
                }
            }
            return fields
        } else {
            return null
        }
    }

    private fun valided(type: Class<*>): Boolean {
        return if (type.classLoader != null) {
            !(HttpServletRequest::class.java.isAssignableFrom(type) || HttpServletResponse::class.java.isAssignableFrom(
                type
            ) || InputStreamSource::class.java.isAssignableFrom(type))
        } else {
            false
        }
    }

    private fun getClassHierarchy(cls: Class<*>): Set<Class<*>> {
        val classes = linkedSetOf<Class<*>>()
        var currentClass: Class<*>? = cls.superclass
        while (currentClass != null && valided(currentClass)) {
            classes.add(currentClass)
            currentClass = currentClass.superclass
        }
        return classes
    }


    private val rootProject: File by lazy {
        var file = FileUtil.userDirFile
        while (!File(file, "gradlew").exists()) {
            file = file.parentFile
        }
        file
    }


    private fun getClassGetters(type: Class<*>, any: Any?): Map<Class<*>, Any?>? {
        if (any is Pageable) {
            return null
        }
        if (any is ResponseEntity<*>) {
            val body = any.body
            if (body != null) {
                val classes = mutableMapOf<Class<*>, Any?>()
                if (body is PagedResources<*>) {
                    val content = body.content
                    if (content != null) {
                        val (cls, value) = extValue(content)
                        if (cls != null && valided(cls)) {
                            classes.put(cls, value)
                        }
                    }
                } else {
                    val (cls, value) = extValue(body)
                    if (cls != null && valided(cls)) {
                        classes.put(cls, value)
                    }
                }
                return classes
            }
        }
        val classes = mutableMapOf<Class<*>, Any?>()
        val beanWrapper = if (any == null) null else BeanWrapperImpl(any)
        val propertyDescriptors =
            if (any == null) Introspector.getBeanInfo(type).propertyDescriptors else beanWrapper!!.propertyDescriptors

        for (pd in propertyDescriptors) {
            val propertyName = pd.name
            val readMethod = pd.readMethod
            if (readMethod != null) {
                val p = beanWrapper?.getPropertyValue(propertyName)
                val returnType = if (p == null) readMethod.returnType else p::class.java
                if (valided(returnType)) {
                    classes.put(returnType, p)
                } else {
                    var componentType: Class<*>? = null
                    var value: Any? = null
                    if (p != null) {
                        val ext = extValue(p)
                        componentType = ext.first
                        value = ext.second
                    }
                    if (componentType == null) {
                        if (returnType.isArray) {
                            componentType = returnType.componentType
                        } else if (Collection::class.java.isAssignableFrom(returnType)
                            || Map::class.java.isAssignableFrom(returnType)
                        ) {
                            componentType = extCollectionOrMap(readMethod)
                        }
                    }
                    if (componentType != null && valided(componentType)) {
                        classes.put(componentType, value)
                    }
                }
            }
        }
        return classes
    }

    private fun extValue(value: Any): Pair<Class<*>?, Any?> {
        if (value is Array<*>) {
            if (value.isNotEmpty()) {
                val p = value.find { it != null }
                if (p != null) {
                    val componentType = p::class.java
                    if (valided(componentType)) {
                        return componentType to p
                    }
                }
            }
        } else if (value is Collection<*>) {
            if (value.isNotEmpty()) {
                val p = value.find { it != null }
                if (p != null) {
                    val componentType = p::class.java
                    if (valided(componentType)) {
                        return componentType to p
                    }
                }
            }
        } else if (value is Map<*, *>) {
            val values = value.values
            if (values.isNotEmpty()) {
                val p = values.find { it != null }
                if (p != null) {
                    val componentType = p::class.java
                    if (valided(componentType)) {
                        return componentType to p
                    }
                }
            }
        } else {
            return value::class.java to value
        }
        return null to null
    }

    private fun extCollectionOrMap(readMethod: Method): Class<*>? {
        val genericType = readMethod.genericReturnType
        if (genericType is ParameterizedType) {
            val actualTypeArguments = genericType.actualTypeArguments
            for (actualTypeArgument in actualTypeArguments) {
                if (actualTypeArgument is Class<*>) {
                    if (valided(actualTypeArgument)) {
                        return actualTypeArgument
                    }
                }
            }
        }
        return null
    }
}
