package top.bettercode.summer.test.autodoc

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.FieldDeclaration
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanWrapperImpl
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.web.PagedResources
import top.bettercode.summer.web.config.summer.WebMvcConfiguration
import java.beans.Introspector
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import javax.persistence.EntityManagerFactory

@Aspect
class AutodocAspect(
    private val entityManagerFactory: List<EntityManagerFactory>,
    private val applicationContext: GenericApplicationContext
) {

    private val log: Logger = LoggerFactory.getLogger(AutodocAspect::class.java)
    private val entityTypes: List<String> by lazy {
        entityManagerFactory.flatMap { it.metamodel.entities.map { it.javaType.simpleName } }
    }

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.PutMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    fun autodoc(joinPoint: ProceedingJoinPoint): Any? {
        val result = joinPoint.proceed()
        if (Autodoc.enable) {
            try {
                val args = joinPoint.args
                val types = linkedSetOf<Class<*>>()
                types.addAll(args.filterNotNull().flatMap {
                    extTypeName(it::class.java, it)
                })
                if (result != null) {
                    types.addAll(extTypeName(result::class.java, result))
                }

                if (types.isNotEmpty()) {
                    val entityTypes = types.filter { isEntity(it.simpleName) }
                    val entityTypeNames = entityTypes.map { it.simpleName }
                    if (entityTypeNames.isNotEmpty()) {
                        if (log.isDebugEnabled)
                            log.debug("自动识别参数类型：{}", entityTypeNames)
                        Autodoc.tableNames.addAll(entityTypeNames)
                    }
                    Autodoc.fields.addAll(fields(entityTypes))
                    val fields = fields(types.filter { !isEntity(it.simpleName) })
                    if (log.isDebugEnabled) {
                        log.debug("自动识别参数：{}", fields)
                    }
                    Autodoc.fields.addAll(fields)
                }
            } catch (e: Exception) {
                log.error("解析参数实体类型失败", e)
            }
        }
        return result
    }

    private fun fields(types: Collection<Class<*>>): List<Field> = types.flatMap {
        val pathname = "src/main/java/${it.name.replace(".", "/")}.java"
        var file = File(pathname)
        if (!file.exists()) {
            rootProject.walkTopDown().find {
                it.isFile && it.absolutePath.endsWith(pathname)
            }?.let {
                file = it
            }
        }
        val fieldComments = mutableListOf<Field>()
        if (file.exists()) {
            val cu = StaticJavaParser.parse(file)
            cu.findAll(FieldDeclaration::class.java).forEach { field ->
                field.variables.forEach { variable ->
                    val fieldName = variable.nameAsString
                    val comment =
                        field.comment.map { it.content.trim().trimStart('*').trim() }.orElse("")
                    if (comment.isNotBlank()) {
                        val type = variable.typeAsString.substringBefore("<")
                        fieldComments.add(
                            Field(
                                name = fieldName,
                                type = type,
                                description = comment
                            )
                        )
                    }
                }
            }
        }
        fieldComments
    }

    val rootProject: File by lazy {
        var file = File("").absoluteFile
        while (!File(file, "gradlew").exists()) {
            file = file.parentFile
        }
        file
    }

    val appPackageName: String by lazy {
        val beanName =
            applicationContext.getBeanNamesForAnnotation(SpringBootApplication::class.java).first()
        val beanDefinition = applicationContext.getBeanDefinition(
            beanName
        ) as AbstractBeanDefinition
        if (!beanDefinition.hasBeanClass()) {
            beanDefinition.resolveBeanClass(
                WebMvcConfiguration::class.java.classLoader
            )
        }
        val beanClass = beanDefinition.beanClass
        beanClass.getPackage().name
    }


    fun isEntity(type: String): Boolean {
        return entityTypes.contains(type)
    }

    private fun extTypeName(type: Class<*>, any: Any?): Set<Class<*>> {
        val result = linkedSetOf<Class<*>>()
        if (type.classLoader != null) {
            getClassHierarchy(type).forEach {
                val simpleName = it.simpleName
                if (it.`package`.name.startsWith(appPackageName) || isEntity(simpleName)) {
                    result.add(it)
                }
            }
            getClassGetters(type, any)?.forEach { cls, value ->
                val simpleName = cls.simpleName
                if (cls.`package`.name.startsWith(appPackageName) || isEntity(simpleName)) {
                    result.add(cls)
                }
                result.addAll(extTypeName(cls, value))
            }
        }
        return result
    }

    private fun valided(type: Class<*>): Boolean {
        return type.classLoader != null
    }

    // 获取一个类的父类列表
    fun getClassHierarchy(cls: Class<*>): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()
        var currentClass: Class<*>? = cls.superclass
        while (currentClass != null && valided(currentClass)) {
            classes.add(currentClass)
            currentClass = currentClass.superclass
        }
        return classes
    }

    fun getClassGetters(type: Class<*>, any: Any?): Map<Class<*>, Any?>? {
        if (any is Pageable) {
            return null
        }
        if (any is ResponseEntity<*>) {
            val body = any.body
            if (body == null) {
                return null
            } else {
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
            val readMethod = pd.getReadMethod()
            if (readMethod != null) {
                val returnType = readMethod.returnType
                val p = beanWrapper?.getPropertyValue(propertyName)
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

    private fun extValue(p: Any): Pair<Class<*>?, Any?> {
        if (p is Array<*>) {
            if (p.isNotEmpty() && p[0] != null) {
                val any = p[0]!!
                val componentType = any::class.java
                if (valided(componentType)) {
                    return componentType to any
                }
            }
        } else if (p is Collection<*>) {
            if (p.isNotEmpty() && p.first() != null) {
                val first = p.first()!!
                val componentType = first::class.java
                if (valided(componentType)) {
                    return componentType to first
                }
            }
        } else if (p is Map<*, *>) {
            if (p.isNotEmpty() && p.values.first() != null) {
                val first = p.values.first()!!
                val componentType = first::class.java
                if (valided(componentType)) {
                    return componentType to first
                }
            }
        } else {
            return p::class.java to p
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
