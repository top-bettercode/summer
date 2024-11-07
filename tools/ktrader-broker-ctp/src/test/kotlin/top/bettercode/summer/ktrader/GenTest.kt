package top.bettercode.summer.ktrader

import org.junit.jupiter.api.Test
import org.rationalityfrontline.jctp.*
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.java.element.Parameter
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.lang.util.JavaType
import java.io.File

/**
 *
 * @author Peter Wu
 */
class GenTest {

    @Test
    fun gen() {
        val clazz = CThostFtdcInstrumentCommissionRateField::class.java
        val type = JavaType("${javaClass.`package`.name}.datatype.InstrumentCommissionRate")
        TopLevelClass(
            type = type,
            overwrite = true
        ).apply {
            method(name = "from", returnType = type, Parameter("field", JavaType(clazz.name))) {
                isStatic = true
                +"${type.shortName} obj = new ${type.shortName}();"
                getBeanProperties(clazz).forEach { it ->
                    val name = it.first
                    +"obj.${name} = field.get${name.replaceFirstChar { it.uppercase() }}();"
                    val javaType = JavaType(it.second)
                    field(name, javaType) {
                        visibility = JavaVisibility.PUBLIC
                    }
                }
                +"return obj;"
            }
        }.writeTo(File("./"))
    }


    fun getBeanProperties(clazz: Class<*>): List<Pair<String, String>> {
        val properties = mutableSetOf<String>()
        val methods = clazz.methods

        // 用来存储发现的 `getter` 和 `setter`
        val types = mutableMapOf<String, String>()
        val getters = mutableSetOf<String>()
        val setters = mutableSetOf<String>()

        // 遍历所有方法，识别 `getter` 和 `setter`
        for (method in methods) {
            val methodName = method.name
            when {
                methodName.startsWith("get") && method.parameterCount == 0 && method.returnType != Void.TYPE -> {
                    // 解析出属性名
                    val propertyName =
                        methodName.substring(3).replaceFirstChar { it.lowercaseChar() }
                    getters.add(propertyName)
                    types[propertyName] = method.returnType.name
                }

                methodName.startsWith("set") && method.parameterCount == 1 -> {
                    val propertyName =
                        methodName.substring(3).replaceFirstChar { it.lowercaseChar() }
                    setters.add(propertyName)
                }
            }
        }

        // 找出同时具有 `getter` 和 `setter` 的属性
        properties.addAll(getters.intersect(setters))

        return properties.map { it to types[it]!! }
    }
}