package plugin

import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.TypeFactory
import org.gradle.api.Project
import top.bettercode.autodoc.core.Util
import top.bettercode.autodoc.core.model.Field
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.InnerInterface
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.Parameter
import top.bettercode.generator.dom.java.element.TopLevelEnumeration
import top.bettercode.generator.dom.unit.FileUnit
import top.bettercode.generator.dom.unit.SourceSet
import top.bettercode.generator.dsl.DicCodes
import java.io.Serializable
import java.util.*


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
        val fields: SortedSet<Field> = TreeSet()
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
        val map = TreeMap<String, DicCodes>()
        val keys = properties.keys.sortedBy {
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
                        ?: code) as Serializable
                dicCode.codes[codeKey] = properties.getProperty(key)
            }
        }
        return map
    }

    private lateinit var docFile: FileUnit
    private val docText = StringBuilder()

    private lateinit var packageName: String


    fun run() {
        docFile = FileUnit(name = "doc/v1.0/编码类型.adoc", replaceable = true,sourceSet = SourceSet.ROOT)

        packageName = project.rootProject.property("app.packageName") as String

        docFile.apply {
            +"== 编码类型"
            +""
            +"""|===
| 编码 | 说明
"""

            codeTypes().forEach { (codeType, v) ->
                val codeTypeName = v.name
                +"|$codeType|$codeTypeName\n"
                docText.appendln(".$codeTypeName($codeType)")
                docText.appendln(
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
                val enumType = JavaType("$packageName.support.dic.${className}Enum")
                val codeEnum = TopLevelEnumeration(enumType)
                codeEnum.visibility = JavaVisibility.PUBLIC
                codeEnum.apply {
                    javadoc {
                        +"/**"
                        +" * ${codeTypeName.replace("@", "\\@")}"
                        +" */"
                    }
                    val innerInterface = InnerInterface(JavaType("${className}Const"))
                    innerInterface(innerInterface)
                    v.codes.forEach { (code, name) ->
                        docText.appendln("|$code|$name")

                        val codeFieldName = underscoreName(
                            if (code is Int || code.toString()
                                    .startsWith("0") && code.toString().length > 1
                            ) {
                                "CODE_${code.toString().replace("-", "MINUS_")}"
                            } else if (code.toString().isBlank()) {
                                "BLANK"
                            } else {
                                code as String
                            }
                        )
                        innerInterface.apply {
                            visibility = JavaVisibility.PUBLIC
                            val initializationString = if (isIntCode) code.toString() else "\"$code\""
                            field(
                                codeFieldName,
                                fieldType,
                                initializationString
                            ) {
                                visibility = JavaVisibility.DEFAULT
                                javadoc {
                                    +"/**"
                                    +" * ${name.replace("@", "\\@")}"
                                    +" */"
                                }
                            }
                        }

                        enumConstant("${codeFieldName}(${className}Const.${codeFieldName})") {
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

                    import("top.bettercode.simpleframework.support.code.CodeServiceHolder")
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
                        +"return CodeServiceHolder.getDefault().getDicCodes(ENUM_NAME).getName(code);"
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
                            +"return (Integer) CodeServiceHolder.getDefault().getDicCodes(ENUM_NAME).getCode(name);"
                        else
                            +"return (String) CodeServiceHolder.getDefault().getDicCodes(ENUM_NAME).getCode(name);"
                    }
                }
                docText.appendln("|===")
                docText.appendln()
                codeEnum.writeTo(project.projectDir)
            }

            +"|===\n"
            +"\n\n"
            +docText.toString()
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


