package top.bettercode.summer.web.support.setting

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor
import org.springframework.cglib.proxy.MethodProxy
import org.springframework.core.convert.ConversionService
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import top.bettercode.summer.tools.lang.property.MapPropertySource
import top.bettercode.summer.tools.lang.property.PropertySource
import java.lang.reflect.Method

/**
 * 设置
 *
 * @author Peter Wu
 */
class Setting private constructor(private val source: PropertySource) {
    private val conversionService: ConversionService = ApplicationConversionService.getSharedInstance()

    operator fun get(key: String?): String? {
        return source[key!!]
    }

    fun getOrDefault(key: String?, defaultValue: String?): String {
        return source.getOrDefault(key!!, defaultValue!!)
    }

    /**
     * 设置配置
     *
     * @param key   配置项
     * @param value 值
     */
    fun put(key: String?, value: String?) {
        source.put(key!!, value)
    }

    /**
     * 删除配置项
     *
     * @param key 配置项
     */
    fun remove(key: String?) {
        source.remove(key!!)
    }

    /**
     * 根据ConfigurationProperties注解绑定配置
     *
     * @param target the target bindable
     * @param <T>    the bound type
     * @return the binding proxy result (never `null`)
    </T> */
    fun <T> bind(target: Class<T>): T {
        val annotation = target.getAnnotation(ConfigurationProperties::class.java)
        Assert.notNull(annotation, target.name + "无ConfigurationProperties注解")
        var prefix = annotation.value
        if (!StringUtils.hasText(prefix)) {
            prefix = annotation.prefix
        }
        return bind(prefix, target)
    }

    /**
     * 绑定配置
     *
     * @param name   the configuration property name to bind
     * @param target the target bindable
     * @param <T>    the bound type
     * @return the binding proxy result (never `null`)
    </T> */
    fun <T> bind(name: String, target: Class<T>?): T {
        val enhancer = Enhancer()
        enhancer.setSuperclass(target)
        enhancer.setCallback(MethodInterceptor { o: Any, method: Method, objects: Array<Any>, methodProxy: MethodProxy ->
            val methodName = method.name
            if (methodName.startsWith("get") && objects.isEmpty()) {
                return@MethodInterceptor get(name, o, method, objects, methodProxy, methodName.substring(3))
            } else if (methodName.startsWith("is") && objects.isEmpty()) {
                return@MethodInterceptor get(name, o, method, objects, methodProxy, methodName.substring(2))
            } else if (methodName.startsWith("set") && objects.size == 1) {
                val result = methodProxy.invokeSuper(o, objects)
                val propertyName = StringUtils.uncapitalize(methodName.substring(3))
                val key = "$name.$propertyName"
                if (result != null) {
                    val value = conversionService.convert(objects[0], String::class.java)
                    put(key, value)
                } else {
                    remove(key)
                }
                return@MethodInterceptor result
            } else {
                return@MethodInterceptor methodProxy.invokeSuper(o, objects)
            }
        })
        @Suppress("UNCHECKED_CAST")
        return enhancer.create() as T
    }

    @Throws(Throwable::class)
    private operator fun get(name: String, o: Any, method: Method, objects: Array<Any>,
                             methodProxy: MethodProxy, propertyName: String): Any? {
        var pName = propertyName
        pName = StringUtils.uncapitalize(pName)
        val key = "$name.$pName"
        var result: Any? = get(key)
        if (result == null) {
            result = methodProxy.invokeSuper(o, objects)
            if (result != null) {
                put(key, conversionService.convert(result, String::class.java))
            }
        } else {
            result = conversionService.convert(result, method.returnType)
        }
        return result
    }

    companion object {
        @JvmStatic
        fun of(source: PropertySource): Setting {
            return Setting(source)
        }

        @JvmStatic
        fun of(source: MutableMap<String, String>): Setting {
            return Setting(MapPropertySource(source))
        }
    }
}
