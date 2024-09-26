package top.bettercode.summer.tools.hikvision

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.lang.util.JavaTypeResolver
import top.bettercode.summer.tools.lang.util.StringUtil.toCamelCase
import top.bettercode.summer.tools.lang.util.StringUtil.toUnderscore
import java.time.format.DateTimeFormatter

/**
 *
 * @author Peter Wu
 */
class CodeGen {
    @Test
    fun name() {
        System.err.println(DateTimeFormatter.ISO_OFFSET_DATE_TIME.toFormat().toString())
    }

    @Disabled
    @Test
    fun gen() {
        val content = javaClass.getResource("/doc.txt")!!.readText().trimIndent()
        val fullTypeSpecification = "${javaClass.`package`.name}.entity.UserFlowResult"
        TopLevelClass(
            type = JavaType(fullTypeSpecification),
            overwrite = true
        ).apply {

            javadoc {
                +"/**"
                +" * "
                +" */"
            }

            content.split("\n").map { it.trim().trim('|') }.forEach { l ->
                l.split("|").let {
                    val name = it[0].trim().trimStart('+')
                    val javaType = when (val type = it[1].trim()) {
                        "string" -> JavaType.stringInstance
                        "string[]" -> JavaType("java.lang.String[]")
                        "int" -> JavaType.intWrapper
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

                    field(name.toCamelCase(), javaType) {
                        annotation("@com.fasterxml.jackson.annotation.JsonProperty(\"$name\")")
                        visibility = JavaVisibility.PUBLIC
                        javadoc {
                            +"/**"
                            +" * $comment ${if (required == "是") "必填" else ""}"
                            +" */"
                        }
                    }

                    System.err.println(
                        "${
                            name.toUnderscore().lowercase()
                        } : ${JavaTypeResolver.calculateJdbcTypeName(javaType)} ${if (required == "是") "NOT " else ""}NULL -- $comment"
                    )
                }
            }
        }
//            .writeTo(File("./"))
    }
}