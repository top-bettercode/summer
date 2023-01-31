package top.bettercode.summer.gradle.plugin.project

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.lang.capitalized

/**
 * @author Peter Wu
 */
open class ExcelCodePrint : ProjectGenerator() {

    override fun call() {
        val code = clazz(controllerType) {
            import("top.bettercode.summer.tools.lang.util.ArrayUtil")
            field(
                "excelFields",
                JavaType("top.bettercode.util.excel.ExcelField<${if (isFullComposite) primaryKeyClassName else className}, ?>[]"),
                isFinal = true
            ) {
                initializationString = "ArrayUtil.of(\n"
                val size = columns.size
                columns.forEachIndexed { i, it ->
                    val code =
                        if (it.isCodeField) {
                            if (it.columnName.contains("_") || it.softDelete) ".code()" else ".code(${
                                (className + it.javaName.capitalized())
                            }Enum.ENUM_NAME)"
                        } else {
                            ""
                        }
                    val propertyGetter =
                        "${if (isFullComposite) primaryKeyClassName else className}::get${it.javaName.capitalized()}"
                    initializationString += "      ExcelField.of(\"${
                        it.remark.split(
                            Regex(
                                "[:：,， (（]"
                            )
                        )[0]
                    }\", $propertyGetter)${code}${if (i == size - 1) "" else ","}\n"
                }

                initializationString += "  )"
            }
            //export
            method("export", JavaType.voidPrimitiveInstance) {
                this.exception(JavaType("java.io.IOException"))
                annotation("@top.bettercode.summer.logging.annotation.RequestLogging(includeResponseBody = false, ignoredTimeout = true)")
                annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/export.xlsx\", name = \"导出\")")
                parameter {
                    type = entityType
                    name = entityName
                }
                parameter {
                    type = JavaType("org.springframework.data.domain.Sort")
                    name = "sort"
                    annotation("@org.springframework.data.web.SortDefault${defaultSort}")
                }

                +"${matcherType.shortName} matcher = ${matcherType.shortName}.matching(${entityName});"
                +"Iterable<$className> results = ${projectEntityName}Service.findAll(matcher, sort);"
                import("top.bettercode.util.excel.ExcelExport")
                +"ExcelExport.sheet(\"$remarks\", excelExport -> excelExport.setData(${
                    if (isFullComposite) {
                        import("com.google.common.collect.Streams")
                        "Streams.stream(results).map($className::get$primaryKeyClassName).collect(Collectors.toList())"
                    } else "results"
                }, excelFields));"
            }

            //template
            method("template", JavaType.voidPrimitiveInstance) {
                this.exception(JavaType("java.io.IOException"))
                annotation("@top.bettercode.summer.logging.annotation.RequestLogging(includeResponseBody = false, ignoredTimeout = true)")
                annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/template.xlsx\", name = \"导入模板\")")

                import("top.bettercode.util.excel.ExcelExport")
                +"ExcelExport.sheet(\"${remarks}导入模板\", excelExport -> excelExport.template(excelFields));"
            }

            //import
            method("importTemplate", JavaType.objectInstance) {
                this.exception(JavaType("java.lang.Exception"))
                annotation("@top.bettercode.summer.logging.annotation.RequestLogging(includeRequestBody = false, ignoredTimeout = true)")
                annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/import\", name = \"导入\")")
                parameter {
                    type = JavaType("org.springframework.web.multipart.MultipartFile")
                    name = "file"
                }

                +"List<${formType.shortName}> list = ExcelImport.of(file).validateGroups(Default.class, CreateConstraint.class).getData(excelFields);"

                +"return noContent();"
            }

            method("export", JavaType.voidPrimitiveInstance) {
//                javadoc {
//                    +"// ${remarks}导出"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"导出\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(1)")
                exception(JavaType("Exception"))
                +"$testInsertName();"
                +"download(get(\"/$pathName/export.xlsx\")"
                2 + ".param(\"sort\", \"\")"
                +");"
            }

            method("template", JavaType.voidPrimitiveInstance) {
                annotation("@org.junit.jupiter.api.DisplayName(\"导入模板\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(1)")
                exception(JavaType("Exception"))
                +"download(get(\"/$pathName/template.xlsx\")"
                +");"
            }

            method("importTemplate", JavaType.voidPrimitiveInstance) {
                annotation("@org.junit.jupiter.api.DisplayName(\"导入\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(1)")
                exception(JavaType("Exception"))
                +"perform(multipart(\"/$pathName/import\")."
                2 + "file(file(\"file\", \"${remarks}导入数据.xlsx\"))"
                +");"
            }

        }.formattedContent
        println(code)
    }
}