package top.bettercode.summer.gradle.plugin.project

import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.TypeFactory
import org.gradle.api.Project
import top.bettercode.summer.tools.autodoc.AutodocUtil
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.*
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import top.bettercode.summer.tools.generator.dsl.DicCodes
import top.bettercode.summer.tools.generator.dsl.Generator.Companion.enumClassName
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.decapitalized
import top.bettercode.summer.tools.lang.util.StringUtil.toUnderscore
import java.io.Serializable
import java.util.*


/**
 * @author Peter Wu
 */
class DicCodeGen(
    private val project: Project,
    private val packageName: String
) {

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
            field.type = t.javaType.shortNameWithoutTypeArguments
            fields.add(field)
        }
        fields.addAll(AutodocUtil.yamlMapper.readValue(file, collectionType))
        file.writeText(AutodocUtil.yamlMapper.writeValueAsString(fields))
    }

    private fun convert(properties: Properties): MutableMap<String, DicCodes> {
        val map = TreeMap<String, DicCodes>()
        val keys = properties.keys.sortedBy {
            if (it.toString().contains(".")) {
                val s = it.toString().substringAfter(".")
                s.toIntOrNull() ?: s.hashCode()
            } else -1
        }
        for (key in keys) {
            key as String
            val codeType: String
            if (key.contains(".")) {
                codeType = key.substringBefore(".")
                val code = key.substringAfter(".")
                var javaType = properties.getProperty("$codeType|TYPE") ?: "java.lang.String"
                if (javaType == "Int") {
                    javaType = JavaType.int.fullyQualifiedNameWithoutTypeParameters
                } else if (javaType == "String") {
                    javaType = JavaType.stringInstance.fullyQualifiedNameWithoutTypeParameters
                }
                val dicCode = map.computeIfAbsent(codeType) {
                    DicCodes(
                        codeType,
                        properties.getProperty(codeType),
                        JavaType(javaType)
                    )
                }
                val codeKey: Serializable =
                    if (code.startsWith("0") && code.length > 1) code else (code.toIntOrNull()
                        ?: code)
                dicCode.codes[codeKey] = properties.getProperty(key)
            }
        }
        return map
    }

    private lateinit var docFile: FileUnit
    private val docText = StringBuilder()


    fun run() {
        docFile = FileUnit("doc/v1.0/编码类型.adoc")

        docFile.apply {
            +"== 编码类型"
            +""
            +"""|===
| 编码 | 说明
"""

            codeTypes().forEach { (codeType, v) ->
                val codeTypeName = v.name
                +"|$codeType|$codeTypeName\n"
                docText.appendLine(".$codeTypeName($codeType)")
                docText.appendLine(
                    """|===
            | 编码 | 说明
            """
                )
                val className = enumClassName(codeType)

                val fieldType = v.javaType

                //
                val enumType = JavaType("$packageName.support.dic.${className}Enum")
                val codeEnum = TopLevelEnumeration(type = enumType, overwrite = true)
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
                        docText.appendLine("|$code|$name")

                        val codeFieldName = (
                                if (code is Int || code.toString()
                                        .startsWith("0") && code.toString().length > 1
                                ) {
                                    "CODE_${code.toString().replace("-", "MINUS_")}"
                                } else if (code.toString().isBlank()) {
                                    "BLANK"
                                } else {
                                    (code as String).replace("-", "_").replace(".", "_")
                                        .toUnderscore()
                                }
                                ).replace(Regex("_+"), "_")
                        innerInterface.apply {
                            visibility = JavaVisibility.PUBLIC
                            val initializationString =
                                when (fieldType) {
                                    JavaType.stringInstance -> "\"$code\""
                                    JavaType.char -> "(char) $code"
                                    else -> code.toString()
                                }
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

                        //auth
                        if ("securityScope" == codeType) {
                            val directory = project.projectDir
                            val authName = code.toString().capitalized()

                            val codeTypeClassName = codeType.capitalized()
                            val authClassName = "Auth${authName}"
                            Interface(
                                type = JavaType("$packageName.security.auth.$authClassName"),
                                overwrite = true
                            ).apply {
                                isAnnotation = true
                                javadoc {
                                    +"/**"
                                    +" * $name 权限标识"
                                    +" *"
                                    +" */"
                                }

                                import("java.lang.annotation.ElementType")
                                annotation("@java.lang.annotation.Target({ElementType.METHOD, ElementType.TYPE})")

                                import("java.lang.annotation.RetentionPolicy")
                                annotation("@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)")
                                annotation("@java.lang.annotation.Inherited")
                                annotation("@java.lang.annotation.Documented")

                                import("$packageName.support.dic.${codeTypeClassName}Enum.${codeTypeClassName}Const")
                                annotation("@top.bettercode.summer.security.authorize.ConfigAuthority(${className}Const.$codeFieldName)")

                            }.writeTo(directory)
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

                    import("top.bettercode.summer.web.support.code.CodeServiceHolder")
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
                    method(
                        "equals",
                        JavaType.boolean,
                        Parameter("code", fieldType.primitiveTypeWrapper ?: fieldType)
                    ) {
                        javadoc {
                            +"/**"
                            +" * @param code 码"
                            +" * @return code 是否相等"
                            +" */"
                        }
                        if (fieldType.isPrimitive)
                            +"return code != null && this.code == code;"
                        else
                            +"return this.code.equals(code);"
                    }
                    method(
                        "enumOf", enumType, Parameter(
                            "code", fieldType.primitiveTypeWrapper
                                ?: fieldType
                        )
                    ) {
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
                        +"for (${className}Enum ${className.decapitalized()}Enum : values()) {"
                        if (fieldType.isPrimitive)
                            +"if (${className.decapitalized()}Enum.code == code) {"
                        else
                            +"if (${className.decapitalized()}Enum.code.equals(code)) {"
                        +"return ${className.decapitalized()}Enum;"
                        +"}"
                        +"}"
                        +"return null;"
                    }
                    method(
                        "nameOf", JavaType.stringInstance, Parameter(
                            "code", fieldType.primitiveTypeWrapper
                                ?: fieldType
                        )
                    ) {
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
                    method(
                        "codeOf", fieldType.primitiveTypeWrapper
                            ?: fieldType, Parameter("name", JavaType.stringInstance)
                    ) {
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
                        +"return (${
                            (fieldType.primitiveTypeWrapper
                                ?: fieldType).shortNameWithoutTypeArguments
                        }) CodeServiceHolder.getDefault().getDicCodes(ENUM_NAME).getCode(name);"
                    }
                }
                docText.appendLine("|===")
                docText.appendLine()
                codeEnum.writeTo(project.projectDir)
            }

            +"|===\n"
            +"\n\n"
            +docText.toString()
        }
    }

}


