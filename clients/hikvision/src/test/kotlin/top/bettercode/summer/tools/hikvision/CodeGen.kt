package top.bettercode.summer.tools.hikvision

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.dom.java.element.Parameter
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.util.JavaType
import java.io.File

/**
 *
 * @author Peter Wu
 */
class CodeGen {

    @Test
    fun gen() {
        val content =javaClass.getResource("doc.txt")!!.readText().trimIndent()
        TopLevelClass(
            type = JavaType("com.cdwintech.app.support.hikvision.entity.EventData"),
            overwrite = true
        ).apply {
            javadoc {
                +"/**"
                +" * 门禁点事件返回参数"
                +" */"
            }

            content.split("\n").map { it.trim().trim('|') }.forEach { l ->
                l.split("|").let {
                    val name = it[0].trim().trimStart('+')
                    val javaType = when (val type = it[1].trim()) {
                        "string" -> JavaType.stringInstance
                        "string[]" -> JavaType("java.lang.String[]")
                        "integer" -> JavaType.intWrapper
                        "integer[]" -> JavaType("java.lang.Integer[]")
                        "number" -> JavaType.intWrapper
                        "number[]" -> JavaType("java.lang.Integer[]")
                        "boolean" -> JavaType.booleanWrapper
                        "object" -> JavaType.objectInstance
                        "object[]" -> JavaType("java.util.List").typeArgument(JavaType.objectInstance)
                        else -> throw IllegalArgumentException("不支持的类型：$type")
                    }
                    val required = it[2].trim()
                    val comment = it[3].trim()

                    field(name, javaType) {
                        javadoc {
                            +"/**"
                            +" * $comment ${if (required == "True") "必填" else ""}"
                            +" */"
                        }
                    }

                    method("get${name.capitalized()}", javaType) {
                        javadoc {
                            +"/**"
                            +" * @return $comment"
                            +" */"
                        }

                        +"return ${name};"
                    }
                    method(
                        "set${name.capitalized()}",
                        JavaType.void,
                        Parameter(name, javaType)
                    ) {
                        javadoc {
                            +"/**"
                            +" * @param $name $comment ${if (required == "True") "必填" else ""}"
                            +" */"
                        }

                        +"this.$name = $name;"
                    }
                }
            }
        }.writeTo(File(""))
    }
}