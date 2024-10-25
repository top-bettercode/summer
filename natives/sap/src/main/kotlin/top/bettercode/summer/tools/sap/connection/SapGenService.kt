package top.bettercode.summer.tools.sap.connection

import com.sap.conn.jco.ConversionException
import com.sap.conn.jco.JCoField
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import org.springframework.util.CollectionUtils
import top.bettercode.summer.tools.generator.dom.java.element.Field
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.java.element.Method
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.generator.dom.unit.SourceSet
import top.bettercode.summer.tools.lang.CharSequenceExtensions.capitalized
import top.bettercode.summer.tools.lang.operation.PrettyPrintingContentModifier.modifyContent
import top.bettercode.summer.tools.lang.util.FileUtil
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.lang.util.JavaType.Companion.dateInstance
import top.bettercode.summer.tools.lang.util.JavaType.Companion.stringInstance
import top.bettercode.summer.tools.lang.util.StringUtil.toCamelCase
import java.io.File
import java.nio.file.Files
import java.util.*

/**
 * @author Peter Wu
 */
class SapGenService(private val sapService: SapService) {
    private val log = LoggerFactory.getLogger(SapGenService::class.java)
    private var outputDir = File(FileUtil.userDir + "/build/sap/")
    fun setOutputDir(outputDir: String): SapGenService {
        Assert.notNull(outputDir, "outputDir must not be null")
        this.outputDir = File(outputDir)
        return this
    }

    fun gen(pojoName: String, functionName: String) {
        outputDir.deleteRecursively()
        val function = sapService.getFunction(functionName)
        if (log.isInfoEnabled) {
            log.info(modifyContent(function.toXML()))
        }
        val classType = JavaType(
            ("SAP_" + pojoName + "_Service").toCamelCase(true)
        )
        val topLevelClass = TopLevelClass(
            classType, true, SourceSet.ROOT,
            JavaVisibility.PUBLIC
        )
        var field = Field()
        field.name = pojoName.uppercase() + "_FUNCTION_NAME"
        field.isStatic = true
        field.isFinal = true
        field.type = stringInstance
        field.initializationString = "\"" + functionName + "\""
        field.visibility = JavaVisibility.PRIVATE
        topLevelClass.field(field)
        field = Field()
        topLevelClass.importedTypes.add(JavaType("org.slf4j.LoggerFactory"))
        field.initializationString = "LoggerFactory.getLogger(" + classType.shortName + ".class)"
        field.type = JavaType("org.slf4j.Logger")
        field.name = "log"
        field.isFinal = true
        field.visibility = JavaVisibility.PRIVATE
        topLevelClass.field(field)
        field = Field()
        val sapServiceType = JavaType("top.bettercode.summer.tools.sap.connection.SapService")
        field.type = sapServiceType
        field.name = "sapService"
        field.isFinal = true
        field.visibility = JavaVisibility.PRIVATE
        topLevelClass.field(field)
        val method = Method()
        method.isConstructor = true
        method.name = classType.shortName
        method.visibility = JavaVisibility.PUBLIC
        method.parameter(sapServiceType, "sapService")
        method.bodyLine("this.sapService = sapService;")
        topLevelClass.method(method)
        topLevelClass.writeTo(outputDir)
        val importParameterList = function.importParameterList
        val exportParameterList = function.exportParameterList
        val tableParameterList = function.tableParameterList
        val inputTables: MutableList<JCoField?> = ArrayList()
        if (importParameterList != null) {
            for (jCoField in importParameterList) {
                inputTables.add(jCoField)
            }
        }
        val outputTables: MutableList<JCoField?> = ArrayList()
        if (exportParameterList != null) {
            for (jCoField in exportParameterList) {
                outputTables.add(jCoField)
            }
        }
        val tables: MutableList<JCoField?> = ArrayList()
        if (tableParameterList != null) {
            for (jCoField in tableParameterList) {
                val description = jCoField.description
                val name = jCoField.name
                if (description.contains("输入") || name.startsWith("IT_")) {
                    inputTables.add(jCoField)
                } else if (description.contains("输出") || name.startsWith("ET_")) {
                    outputTables.add(jCoField)
                } else {
                    tables.add(jCoField)
                }
            }
        }
        val properties = Properties()
        genClass(pojoName, "Req", "", false, inputTables, properties)
        genClass(pojoName, "Resp", "", false, outputTables, properties)
        genClass(
            pojoName, if (CollectionUtils.isEmpty(outputTables)) "Resp" else "Tables", "", false,
            tables, properties
        )
        val file = File(outputDir, "$pojoName.properties")
        properties.store(Files.newBufferedWriter(file.toPath()), "SAP POJO properties")
        log.info("生成：" + file.name)
    }

    private fun genClass(
        pojoName: String, name: String, desc: String, exist: Boolean,
        jCoFields: Iterable<JCoField?>?, properties: Properties
    ) {
        if (jCoFields == null || !jCoFields.iterator().hasNext()) {
            return
        }
        val classType = JavaType(
            (if (exist) "pojo." else "") + (pojoName + "_" + name).toCamelCase(true)
        )
        val topLevelClass = TopLevelClass(
            classType, true, SourceSet.ROOT, JavaVisibility.PUBLIC
        )
        var write = false
        loop@ for (jCoField in jCoFields) {
            val jcoFieldName = jCoField!!.name
            val description = jCoField.description
            var fieldName = jcoFieldName.toCamelCase()
            properties.setProperty(fieldName, description)
            var annotation: String
            var initializationString: String? = null
            var type: JavaType?
            val javaType = JavaType(
                (pojoName + "_" + jcoFieldName).toCamelCase(true)
            )
            if (jCoField.isStructure) {
                annotation =
                    "@top.bettercode.summer.tools.sap.annotation.SapStructure(\"$jcoFieldName\")"
                when (jcoFieldName) {
                    "IS_ZSCRM2_CONTROL" -> {
                        genClass(
                            pojoName, jcoFieldName, description, true, jCoField.structure,
                            properties
                        )
                        type = JavaType("top.bettercode.summer.tools.sap.connection.pojo.SapHead")
                        fieldName = "head"
                    }

                    "ES_MESSAGE" -> {
                        genClass(
                            pojoName, jcoFieldName, description, true, jCoField.structure,
                            properties
                        )
                        topLevelClass.superClass(
                            JavaType(
                                "top.bettercode.summer.tools.sap.connection.pojo.SapReturn"
                            ).typeArgument(
                                "top.bettercode.summer.tools.sap.connection.pojo.RkEsMessage"
                            )
                        )
                        continue@loop
                    }

                    else -> {
                        type = javaType
                        genClass(
                            pojoName, jcoFieldName, description, false, jCoField.structure,
                            properties
                        )
                    }
                }
            } else if (jCoField.isTable) {
                annotation =
                    "@top.bettercode.summer.tools.sap.annotation.SapTable(\"$jcoFieldName\")"
                if ("ET_RETURN" == jcoFieldName) {
                    topLevelClass.superClass("top.bettercode.summer.tools.sap.connection.pojo.EtReturns")
                    genClass(pojoName, jcoFieldName, description, true, jCoField.table, properties)
                    continue
                } else {
                    type = JavaType("java.util.List").typeArgument(javaType)
                    genClass(pojoName, jcoFieldName, description, false, jCoField.table, properties)
                }
            } else {
                annotation =
                    "@top.bettercode.summer.tools.sap.annotation.SapField(\"$jcoFieldName\")"
                var value: Any? = null
                try {
                    value = jCoField.value
                } catch (ignored: Exception) {
                }
                when (val jCoFieldType = jCoField.type) {
                    0, 6, 29 -> {
                        type = stringInstance
                        if (value != null && "" != value) {
                            initializationString = "\"" + value + "\""
                        }
                    }

                    1, 3 -> type = dateInstance
                    2, 23, 24 -> {
                        type = JavaType("java.math.BigDecimal")
                        if (value != null) {
                            initializationString = "new BigDecimal(\"$value\")"
                        }
                    }

                    4, 30 -> type = JavaType("byte[]")
                    7 -> type = JavaType("java.lang.Double")
                    8, 9, 10 -> {
                        type = JavaType("java.lang.Integer")
                        if (value != null) {
                            initializationString = value.toString()
                        }
                    }

                    16 -> type = JavaType("com.sap.conn.jco.rt.DefaultAbapObject")
                    17 -> type = JavaType("com.sap.conn.jco.rt.DefaultStructure")
                    99 -> type = JavaType("com.sap.conn.jco.rt.DefaultTable")
                    else -> throw ConversionException(
                        jcoFieldName + " unsupported type: " + jCoFieldType + "("
                                + jCoField.typeAsString + ")"
                    )
                }
            }
            write = true
            val field = Field()
            field.initializationString = initializationString
            field.annotation(annotation)
            field.type = type
            field.name = fieldName
            field.javadoc("/**", " * $description", " */")
            val getMethod = Method()
            getMethod.visibility = JavaVisibility.PUBLIC
            getMethod.name = "get" + fieldName.capitalized()
            getMethod.javadoc("/**", " * @return $description", " */")
            getMethod.bodyLine("return this.$fieldName;")
            val setMethod = Method()
            setMethod.visibility = JavaVisibility.PUBLIC
            setMethod.name = "set" + fieldName.capitalized()
            setMethod.javadoc(
                "/**",
                " * 设置$description",
                " *",
                " * @param $fieldName $description",
                " * @return " + desc.ifBlank { classType.shortName },
                " */"
            )
            setMethod.bodyLine("this.$fieldName = $fieldName;")
            setMethod.bodyLine("return this;")
            getMethod.returnType = type
            setMethod.returnType = classType
            setMethod.parameter(type, fieldName)
            field.visibility = JavaVisibility.PRIVATE
            topLevelClass.field(field)
            topLevelClass.method(getMethod)
            topLevelClass.method(setMethod)
        }
        if (write) {
            val toStringMethod = Method()
            toStringMethod.visibility = JavaVisibility.PUBLIC
            toStringMethod.name = "toString"
            toStringMethod.returnType = stringInstance
            toStringMethod.annotation("@Override")
            toStringMethod.bodyLine("return StringUtil.json(this);")
            topLevelClass.importedTypes
                .add(JavaType("top.bettercode.summer.tools.lang.util.StringUtil"))
            topLevelClass.method(toStringMethod)
            topLevelClass.writeTo(outputDir)
        }
    }
}
