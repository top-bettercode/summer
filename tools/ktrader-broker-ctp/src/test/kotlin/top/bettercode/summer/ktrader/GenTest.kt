package top.bettercode.summer.ktrader

import org.junit.jupiter.api.Test
import org.rationalityfrontline.jctp.CThostFtdcDepthMarketDataField
import java.io.File

/**
 *
 * @author Peter Wu
 */
class GenTest {

    @Test
    fun gen() {
        val clazz = CThostFtdcDepthMarketDataField::class.java
        val type = "DepthMarketData"

        val imports = mutableSetOf<String>()
        val printWriter =
            File("src/main/kotlin/top/bettercode/summer/ktrader/datatype/$type.kt").printWriter()
        printWriter.use { out ->
            imports.add("import ${clazz.name}")

            out.appendLine(
                """package top.bettercode.summer.ktrader.datatype

${imports.sorted().joinToString("\n")}

/**
 * @author Peter Wu
 */
data class $type("""
            )
            val properties = getBeanProperties(clazz)
            properties.forEach {
                when (val ptype = it.second) {
                    "char" -> out.appendLine("    var ${it.first}: Char = 0.toChar(),")
                    "int" -> out.appendLine("    var ${it.first}: Int = 0,")
                    "double" -> out.appendLine("    var ${it.first}: Double = 0.0,")
                    "long" -> out.appendLine("    var ${it.first}: Long = 0,")
                    else -> out.appendLine("    var ${it.first}: $ptype? = null,")
                }

            }
            out.appendLine(
                """) {
    companion object {
        fun from(field: ${clazz.simpleName}): $type {
            val obj = $type()
${
                    properties.joinToString("\n") {
                        "            obj.${it.first} = field.${it.first}"
                    }
                }
            return obj
        }
    }
}
"""
            )
        }
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
                    types[propertyName] = method.returnType.simpleName
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