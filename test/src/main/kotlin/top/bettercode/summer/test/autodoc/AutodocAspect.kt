package top.bettercode.summer.test.autodoc

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanWrapperImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import top.bettercode.summer.web.PagedResources
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import javax.persistence.EntityManagerFactory

@Aspect
class AutodocAspect(private val entityManagerFactory: EntityManagerFactory) {

    private val log: Logger = LoggerFactory.getLogger(AutodocAspect::class.java)
    private val entityTypes: List<Class<*>> by lazy {
        entityManagerFactory.metamodel.entities.map { it.javaType }
    }

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.PutMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    fun autodoc(joinPoint: ProceedingJoinPoint): Any? {
        val result = joinPoint.proceed()
        if (Autodoc.enable) {
            try {
                val args = joinPoint.args
                val typeNames = mutableListOf<String>()
                typeNames.addAll(args.filterNotNull().flatMap {
                    extTypeName(it)
                })
                if (result != null) {
                    typeNames.addAll(extTypeName(result))
                }

                if (typeNames.isNotEmpty()) {
                    log.debug("自动识别参数类型：{}", typeNames)
                    Autodoc.tableNames.addAll(typeNames)
                }
            } catch (e: Exception) {
                log.error("解析参数实体类型失败", e)
            }
        }
        return result
    }

    private fun extTypeName(any: Any): List<String> {
        val result = mutableListOf<String>()
        val type = any::class.java
        if (type.classLoader != null) {
            getClassHierarchy(type).forEach {
                if (entityTypes.contains(it)) {
                    result.add(it.simpleName)
                }
            }
            getClassGetters(any)?.forEach {
                if (entityTypes.contains(it)) {
                    result.add(it.simpleName)
                }
                result.addAll(extTypeName(it))
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

    fun getClassGetters(any: Any): List<Class<*>>? {
        if (any is Pageable) {
            return null
        }
        if (any is ResponseEntity<*>) {
            val body = any.body
            if (body == null) {
                return null
            } else {
                val classes = mutableListOf<Class<*>>()
                if (body is PagedResources<*>) {
                    val content = body.content
                    if (content != null) {
                        val type = extValue(content)
                        if (type != null && valided(type)) {
                            classes.add(type)
                        }
                    }
                } else {
                    val type = extValue(body)
                    if (type != null && valided(type)) {
                        classes.add(type)
                    }
                }
                return classes
            }
        }
        val classes = mutableListOf<Class<*>>()
        val beanWrapper = BeanWrapperImpl(any)
        val propertyDescriptors = beanWrapper.propertyDescriptors

        for (pd in propertyDescriptors) {
            val propertyName = pd.name
            val readMethod = pd.getReadMethod()
            if (readMethod != null) {
                val returnType = readMethod.returnType
                if (valided(returnType)) {
                    classes.add(returnType)
                } else {
                    val p = beanWrapper.getPropertyValue(propertyName)
                    var componentType: Class<*>? = null
                    if (p != null)
                        componentType = extValue(p)
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
                        classes.add(componentType)
                    }
                }
            }
        }
        return classes
    }

    private fun extValue(p: Any): Class<*>? {
        if (p is Array<*>) {
            if (p.isNotEmpty() && p[0] != null) {
                val componentType = p[0]!!::class.java
                if (valided(componentType)) {
                    return componentType
                }
            }
        } else if (p is Collection<*>) {
            if (p.isNotEmpty() && p.first() != null) {
                val componentType = p.first()!!::class.java
                if (valided(componentType)) {
                    return componentType
                }
            }
        } else if (p is Map<*, *>) {
            if (p.isNotEmpty() && p.values.first() != null) {
                val componentType = p.values.first()!!::class.java
                if (valided(componentType)) {
                    return componentType
                }
            }
        } else {
            return p::class.java
        }
        return null
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
