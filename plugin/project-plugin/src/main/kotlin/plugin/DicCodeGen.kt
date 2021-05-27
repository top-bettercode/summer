package plugin

import cn.bestwu.autodoc.core.Util
import cn.bestwu.autodoc.core.model.Field
import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.InnerClass
import cn.bestwu.generator.dom.java.element.JavaVisibility
import cn.bestwu.generator.dom.java.element.Parameter
import cn.bestwu.generator.dom.java.element.TopLevelEnumeration
import cn.bestwu.generator.dsl.DicCodes
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.TypeFactory
import org.gradle.api.Project
import java.io.File
import java.util.Properties


/**
 * @author Peter Wu
 */
class DicCodeGen(private val project: Project) {


    private fun codeTypes(): Map<String, DicCodes> {
        val properties = Properties()
        val defaultDicCodeFile = project.file("src/main/resources/default-dic-code.properties")
        if (defaultDicCodeFile.exists())
            properties.load(defaultDicCodeFile.inputStream())
        val dicCodeFile = project.file("src/main/resources/dic-code.properties")
        if (dicCodeFile.exists()) {
            properties.load(dicCodeFile.inputStream())

            val props = Properties()
            props.load(dicCodeFile.inputStream())
            val collectionType = TypeFactory.defaultInstance()
                .constructCollectionType(LinkedHashSet::class.java, Field::class.java)
            addFields("doc/request.parameters.yml", props, collectionType)
            addFields("doc/response.content.yml", props, collectionType)
        }
        return convert(properties)
    }

    private fun addFields(filePath: String, props: Properties, collectionType: CollectionType?) {
        val file = project.rootProject.file(filePath)
        val fields: LinkedHashSet<Field> = linkedSetOf()
        convert(props).values.forEach { t ->
            val field = Field()
            field.name = t.type
            field.description =
                "${t.name}(${t.codes.entries.joinToString { "${it.key}:${it.value}" }})"
            field.type = if (t.isInt) "Integer" else "String"
            fields.add(field)
        }
        fields.addAll(Util.yamlMapper.readValue(file, collectionType))
        file.writeText(Util.yamlMapper.writeValueAsString(fields))
    }

    private fun convert(properties: Properties): MutableMap<String, DicCodes> {
        val map = mutableMapOf<String, DicCodes>()
        val keys = properties.keys.toList().sortedBy {
            if (it.toString().contains(".")) {
                val s = it.toString().split(".")[1]
                s.toIntOrNull() ?: s.hashCode()
            } else -1
        }
        for (key in keys) {
            key as String
            val codeType: String
            if (key.contains(".")) {
                val split = key.split(".")
                codeType = split[0]
                val code = split[1]
                val isInt = "Int" == properties.getProperty("$codeType|TYPE")
                val dicCode = map.computeIfAbsent(codeType) {
                    DicCodes(
                        codeType,
                        properties.getProperty(codeType),
                        isInt
                    )
                }
                val codeKey =
                    if (code.startsWith("0") && code.length > 1) code else (code.toIntOrNull()
                        ?: code)
                dicCode.codes[codeKey] = properties.getProperty(key)
            }
        }
        return map
    }

    private lateinit var docFile: File
    private val docText = StringBuilder()

    private lateinit var packageName: String


    fun setUp() {
        docFile = project.rootProject.file("doc/v1.0/编码类型.adoc")
        if (!docFile.parentFile.exists())
            docFile.parentFile.mkdirs()
        packageName = project.rootProject.property("summer.packageName") as String

        docFile.printWriter().use { pw ->
            pw.println("== 编码类型")
            pw.println("")
            pw.println(
                """|===
| 编码 | 说明
"""
            )
        }
    }

    fun tearDown() {
        docFile.appendText("|===\n")
        docFile.appendText("\n\n")
        docFile.appendText(docText.toString())
    }

    fun genCode() {
        codeTypes().forEach { (codeType, v) ->
            val codeTypeName = v.name
            docFile.appendText("|$codeType|$codeTypeName\n")
            docText.appendLine(".$codeTypeName($codeType)")
            docText.appendLine(
                """|===
| 编码 | 说明
"""
            )
            val className = codeType.split("_").joinToString("") {
                if (codeType.matches(Regex(".*[a-z].*")))
                    it.capitalize()
                else
                    it.toLowerCase().capitalize()
            }

            val isIntCode = v.isInt
            val fieldType =
                if (isIntCode) JavaType.intPrimitiveInstance else JavaType.stringInstance
            val fieldType2 =
                if (isIntCode) JavaType("java.lang.Integer") else JavaType.stringInstance

            //
            val enumFile = project.file(
                "src/main/java/${
                    packageName.replace(
                        '.',
                        '/'
                    )
                }/support/dic/${className}Enum.java"
            )
            val enumType = JavaType("$packageName.support.dic.${className}Enum")
            val codeEnum = TopLevelEnumeration(enumType)
            codeEnum.visibility = JavaVisibility.PUBLIC
            codeEnum.apply {
                javadoc {
                    +"/**"
                    +" * ${codeTypeName.replace("@", "\\@")}"
                    +" */"
                }
                val innerClass = InnerClass(JavaType("Const"))
                innerClass(innerClass)
                v.codes.forEach { (code, name) ->
                    docText.appendLine("|$code|$name")

                    val codeFieldName = underscoreName(
                        if (code is Int || code.toString()
                                .startsWith("0") && code.toString().length > 1
                        ) {
                            "CODE_${code.toString().replace("-", "MINUS_")}"
                        } else {
                            code as String
                        }
                    )
                    innerClass.apply {
                        visibility = JavaVisibility.PUBLIC
                        isStatic = true
                        val initializationString = if (isIntCode) code.toString() else "\"$code\""
                        field(
                            codeFieldName,
                            fieldType,
                            initializationString,
                            true,
                            JavaVisibility.PUBLIC
                        ) {
                            isStatic = true
                            javadoc {
                                +"/**"
                                +" * ${name.replace("@", "\\@")}"
                                +" */"
                            }
                        }
                    }

                    enumConstant("${codeFieldName}(Const.${codeFieldName})") {
                        javadoc {
                            +"/**"
                            +" * ${name.replace("@", "\\@")}"
                            +" */"
                        }
                    }

                }

                field(
                    "ENUM_NAME",
                    JavaType.stringInstance,
                    "\"$codeType\"",
                    true,
                    JavaVisibility.PUBLIC
                ) {
                    isStatic = true
                    javadoc {
                        +"/**"
                        +" * ${codeTypeName.replace("@", "\\@")}"
                        +" */"
                    }
                }

                this.constructor(visibility = JavaVisibility.DEFAULT) {
                    parameter(fieldType, "code")
                    +"this.code = code;"
                }

                field("code", fieldType, visibility = JavaVisibility.PRIVATE, isFinal = true)
                method("code", fieldType) {
                    +"return code;"
                }

                import("cn.bestwu.simpleframework.support.code.CodeTypes")
                method("nameOf", JavaType.stringInstance) {
                    javadoc {
                        +"/**"
                        +" * 对应名称"
                        +" *"
                        +" * @return 对应名称"
                        +" */"
                    }
                    +"return nameOf(code);"
                }
                method("equals", JavaType.booleanPrimitiveInstance, Parameter("code", fieldType2)) {
                    javadoc {
                        +"/**"
                        +" * @param code 码"
                        +" * @return code 是否相等"
                        +" */"
                    }
                    if (isIntCode)
                        +"return code != null && this.code == code;"
                    else
                        +"return this.code.equals(code);"
                }
                method("enumOf", enumType, Parameter("code", fieldType2)) {
                    javadoc {
                        +"/**"
                        +" * 根据标识码查询对应枚举"
                        +" *"
                        +" * @param code 标识码"
                        +" * @return 对应枚举"
                        +" */"
                    }
                    isStatic = true
                    +"if (code == null) {"
                    +"return null;"
                    +"}"
                    +"for (${className}Enum ${className.decapitalize()}Enum : values()) {"
                    if (isIntCode)
                        +"if (${className.decapitalize()}Enum.code == code) {"
                    else
                        +"if (${className.decapitalize()}Enum.code.equals(code)) {"
                    +"return ${className.decapitalize()}Enum;"
                    +"}"
                    +"}"
                    +"return null;"
                }
                method("nameOf", JavaType.stringInstance, Parameter("code", fieldType2)) {
                    javadoc {
                        +"/**"
                        +" * 根据标识码查询对应名称"
                        +" *"
                        +" * @param code 标识码"
                        +" * @return 对应名称"
                        +" */"
                    }
                    isStatic = true
                    +"if (code == null) {"
                    +"return null;"
                    +"}"
                    +"return CodeTypes.getCodeService().getName(ENUM_NAME, code);"
                }
                method("codeOf", fieldType2, Parameter("name", JavaType.stringInstance)) {
                    javadoc {
                        +"/**"
                        +" * 根据标识码名称查询对应标识码"
                        +" *"
                        +" * @param name 名称"
                        +" * @return 标识码"
                        +" */"
                    }
                    isStatic = true
                    +"if (name == null) {"
                    +"return null;"
                    +"}"
                    if (isIntCode)
                        +"return (Integer) CodeTypes.getCodeService().getCode(ENUM_NAME, name);"
                    else
                        +"return (String) CodeTypes.getCodeService().getCode(ENUM_NAME, name);"
                }
            }
            docText.appendLine("|===")
            docText.appendLine()
            enumFile.parentFile.mkdirs()
            enumFile.writeText(codeEnum.formattedContent)
        }
    }

    companion object {

        fun underscoreName(name: String): String {
            if (name.matches(Regex(".*[a-z].*"))) {
                val result = StringBuilder()
                if (name.isNotEmpty()) {
                    // 将第一个字符处理成大写
                    result.append(name.substring(0, 1).toUpperCase())
                    // 循环处理其余字符
                    for (i in 1 until name.length) {
                        val s = name.substring(i, i + 1)
                        // 在大写字母前添加下划线
                        if (s == s.toUpperCase() && !Character.isDigit(s[0]) && s[0] != '_') {
                            result.append("_")
                        }
                        // 其他字符直接转成大写
                        result.append(s.toUpperCase())
                    }
                }
                return result.toString()
            } else
                return name
        }
    }
}


