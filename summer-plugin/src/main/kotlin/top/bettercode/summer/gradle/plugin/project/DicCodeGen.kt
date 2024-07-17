package top.bettercode.summer.gradle.plugin.project

import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.dictionary.CustomDictionary
import org.gradle.api.Project
import top.bettercode.summer.tools.autodoc.AutodocUtil
import top.bettercode.summer.tools.autodoc.model.Field
import top.bettercode.summer.tools.generator.dom.java.element.*
import top.bettercode.summer.tools.generator.dom.unit.FileUnit
import top.bettercode.summer.tools.generator.dsl.DicCodes
import top.bettercode.summer.tools.generator.dsl.Generator.Companion.enumClassName
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.decapitalized
import top.bettercode.summer.tools.lang.property.PropertiesSource
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.lang.util.StringUtil.toUnderscore
import java.io.Serializable
import java.util.*


/**
 * @author Peter Wu
 */
class DicCodeGen(
    private val project: Project,
    private val packageName: String,
    private val replaceCodeNames: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
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
                        type = codeType,
                        name = properties.getProperty(codeType),
                        javaType = JavaType(javaType)
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

        project.file("src/main/java/${packageName.replace(".", "/")}/support/dic")
            .deleteRecursively()
        docFile.apply {
            +"== 编码类型"
            +""
            +"""|===
| 编码 | 说明
"""

            codeTypes().forEach { (codeType, dicCodes) ->
                val codeTypeName = dicCodes.name
                +"|$codeType|$codeTypeName\n"
                docText.appendLine(".$codeTypeName($codeType)")
                docText.appendLine(
                    """|===
            | 编码 | 说明
            """
                )
                genCode(dicCodes)
            }

            +"|===\n"
            +"\n\n"
            +docText.toString()
        }
        if (project.findProperty("app.update") == "true") {
            project.logger.lifecycle("更新代码")
            val replaces: MutableMap<String, String> = mutableMapOf()
            replaceCodeNames.forEach { (className, u) ->
                u.forEach { (oldCodeName, codeName) ->
                    replaces["${className}Enum.$oldCodeName"] = "${className}Enum.$codeName"
                    replaces["${className}Const.$oldCodeName"] = "${className}Const.$codeName"
                }
            }
            replaceOld(replaces)
            project.logger.lifecycle("更新代码完成")
        }

    }

    fun genCode(dicCodes: DicCodes, auth: Boolean = false) {
        val codeType: String = dicCodes.type
        val codeTypeName: String = dicCodes.name
        val className = enumClassName(codeType)

        val fieldType = dicCodes.javaType

        //
        val enumType =
            JavaType("$packageName.${if (auth) "security.auth" else "support.dic"}.${className}Enum")
        val codeEnum = TopLevelEnumeration(type = enumType, overwrite = true)
        codeEnum.visibility = JavaVisibility.PUBLIC
        codeEnum.apply {
            javadoc {
                +"/**"
                +" * ${
                    codeTypeName.replace(
                        "@",
                        "\\@"
                    )
                }(${dicCodes.codes.entries.joinToString { "${it.key}:${it.value}" }})"
                +" */"
            }
            val innerInterface = InnerInterface(JavaType("${className}Const")).apply {
                javadoc {
                    +"/**"
                    +" * ${
                        codeTypeName.replace(
                            "@",
                            "\\@"
                        )
                    }(${dicCodes.codes.entries.joinToString { "${it.key}:${it.value}" }})"
                    +" */"
                }
                visibility = JavaVisibility.PUBLIC
            }
            innerInterface(innerInterface)
            val codeFieldNames = mutableSetOf<String>()
            dicCodes.codes.forEach { (code, name) ->
                docText.appendLine("|$code|$name")

                var codeFieldName = codeName(code, name)
                if (codeFieldNames.contains(codeFieldName)) {
                    codeFieldName += "_" + code.toString().replace("-", "_").replace(".", "_")
                        .toUnderscore()
                }
                codeFieldNames.add(codeFieldName)

                val oldCodeName = (
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
                if (oldCodeName != codeFieldName) {
                    val map = replaceCodeNames.computeIfAbsent(className) {
                        mutableMapOf()
                    }
                    map[oldCodeName] = codeFieldName
                }

                innerInterface.apply {
                    field(
                        name = codeFieldName,
                        type = fieldType,
                        initializationString = when (fieldType) {
                            JavaType.stringInstance -> "\"$code\""
                            JavaType.char -> "(char) $code"
                            else -> code.toString()
                        }
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
                        +" * $code : ${name.replace("@", "\\@")}"
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

    fun codeName(code: Serializable, name: String) = (
            when {
                code is Number || code.toString().matches(Regex("\\d.*")) -> {
                    codeName(name)
                }

                code.toString().isBlank() -> {
                    "BLANK"
                }

                else -> {
                    (code as String).replace("-", "_").replace(".", "_")
                        .toUnderscore()
                }
            }
            ).replace(Regex("_+"), "_")

    private val defaultDict = PropertiesSource.of("default-dict")
    private val dict = PropertiesSource.of("dict")
    private val dictMap: Map<String, String>
    private val coreProperties: Map<String, String>

    init {
        val dictFile = project.rootProject.file("puml/dict.properties")
        dictMap = mutableMapOf()
        if (dictFile.exists()) {
            val properties = Properties()
            properties.load(dictFile.inputStream())
            properties.forEach { (t, u) ->
                dictMap[t.toString()] = u.toString()
            }
            dictMap.keys.forEach { key ->
                CustomDictionary.add(key)
            }
        }
        val coreMsgfile = project.file("src/main/resources/core-messages.properties")
        coreProperties = mutableMapOf()
        if (coreMsgfile.exists()) {
            val properties = Properties()
            properties.load(coreMsgfile.inputStream())
            val map = properties.map { (t, u) ->
                val key = u.toString()
                val value = t.toString()
                key to value
            }.groupBy { it.first }
                .mapValues { pair -> pair.value.minByOrNull { it.second.length }?.second }
                .filter { it.value != null }
            map.forEach { (t, u) ->
                coreProperties[t] = u!!
                CustomDictionary.add(t)
            }
        }
        dict.source.keys.forEach { key ->
            CustomDictionary.add(key)
        }
    }

    fun codeName(name: String): String {
        var text =
            name.substringBefore("(").substringBefore("（").substringBefore(",").substringBefore("，")
                .replace("/", "or")
        val regex = Regex("([a-zA-Z0-9]+)")
        text = text.replace(regex, "_$1_")

        var result = text.split(Regex("_+")).filter { it.isNotBlank() }.map { part ->
            if (regex.matches(part)) {
                part
            } else {
                var partText = translator(part) ?: part
                partText = partText.replace(regex, "_$1_")
                partText.split(Regex("_+")).joinToString("_") { pp ->
                    if (regex.matches(pp)) {
                        pp
                    } else {
                        HanLP.segment(pp).joinToString("_") {
                            val word = it.word
                            (translator(word)
                                ?: PinyinHelper.convertToPinyinString(
                                    word,
                                    "_",
                                    PinyinFormat.WITHOUT_TONE
                                ))
                        }
                    }
                }
            }
        }.joinToString("_") {
            it.trim('_').uppercase()
        }
        if (result.startsWith("UN_")) {
            return "UN${result.substringAfter("UN_")}"
        }
        if (result.startsWith("ALREADY_")) {
            result = result.substringAfter("ALREADY_")
            return result.split("_").mapIndexed { index: Int, s: String ->
                if (index == 0) {
                    "${s}ED"
                } else {
                    s
                }
            }.joinToString("_")
        }
        if (result.endsWith("_ING")) {
            result = result.substringBeforeLast("_ING") + "ING"
        }
        if (result.contains("_ING_")) {
            result = result.replace("_ING_", "ING_")
        }
        return result
    }

    private fun translator(key: String): String? {
        val result = dictMap[key] ?: dict[key] ?: coreProperties[key] ?: defaultDict[key]
        return result?.toUnderscore()?.replace(" ", "_")?.replace("-", "_")?.replace("'", "")
    }

    fun replaceOld(replaceCodeNames: MutableMap<String, String>) {
        project.rootDir.walkTopDown()
            .filter { it.isFile && (it.extension == "java" || it.extension == "kt") }
            .forEach { file ->
                val texts = file.readLines()
                file.bufferedWriter().use { pt ->
                    texts.forEach { line ->
                        var text = line
                        replaceCodeNames.forEach { (old, new) ->
                            val regex = """([^a-zA-Z0-9_])\Q${old}\E([^a-zA-Z0-9_]|$)"""
                            if (text.matches(".*${regex}.*".toRegex())) {
                                print(".")
                                text = text.replace(
                                    regex.toRegex(),
                                    "$1${new}$2"
                                )
                            }
                        }
                        pt.appendLine(text)
                    }
                }
            }
    }
}


