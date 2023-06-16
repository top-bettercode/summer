package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.lang.capitalized

/**
 * @author Peter Wu
 */
open class ExcelCodePrint : ProjectGenerator() {

    override fun call() {
        val code = clazz(controllerType) {

            val formClassName = formType.shortName

            val filterColumns = columns.filter { it.javaName != primaryKeyName && !it.testIgnored && (!it.isPrimary || isFullComposite) }

            import("top.bettercode.summer.tools.lang.util.ArrayUtil")
            field(
                    "excelFields",
                    JavaType("top.bettercode.util.excel.ExcelField<${formClassName}, ?>[]"),
                    isFinal = true
            ) {
                initializationString = "ArrayUtil.of(\n"
                val size = filterColumns.size
                filterColumns.forEachIndexed { i, it ->
                    val code =
                            if (it.isCodeField) {
                                if (it.columnName.contains("_") || it.softDelete) ".code()" else ".code(${
                                    it.codeType.capitalized()
                                }Enum.ENUM_NAME).dataValidation(${it.dicCodes()!!.codes.values.joinToString(", ") { s -> "\"$s\"" }})"
                            } else {
                                ""
                            }
                    val propertyGetter =
                            "${formClassName}::get${it.javaName.capitalized()}"
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
            method("export", JavaType.void) {
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
                +"List<$className> results = ${projectEntityName}Service.findAll(matcher, sort);"
                import("top.bettercode.util.excel.ExcelExport")
                +"ExcelExport.sheet(\"$remarks\", excelExport -> excelExport.setData(${
                    if (isFullComposite) {
                        import("com.google.common.collect.Streams")
                        "Streams.stream(results).map($className::get$primaryKeyClassName).collect(Collectors.toList())"
                    } else "results"
                }, excelFields));"
            }

            //template
            method("template", JavaType.void) {
                annotation("@top.bettercode.summer.logging.annotation.RequestLogging(includeResponseBody = false, ignoredTimeout = true)")
                annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/template.xlsx\", name = \"导入模板\")")

                import("top.bettercode.util.excel.ExcelExport")
                +"ExcelExport.sheet(\"${remarks}导入模板\", excelExport -> excelExport.template(excelFields));"
            }

            //import
            method("importTemplate", JavaType.objectInstance) {
                annotation("@top.bettercode.summer.logging.annotation.RequestLogging(includeRequestBody = false, ignoredTimeout = true)")
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/import\", name = \"导入\")")
                parameter {
                    type = JavaType("org.springframework.web.multipart.MultipartFile")
                    name = "file"
                }

                +"List<$formClassName> list = ExcelImport.of(file).validateGroups(Default.class, CreateConstraint.class).getData(excelFields);"
                +"for (UserAdminForm form : list) {"
                +"create(form);"
                +"}"
                +"return noContent();"
            }

            method("export", JavaType.void) {
//                javadoc {
//                    +"// ${remarks}导出"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"导出\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(1)")
                +"$testInsertName();"
                +"download(get(\"/$pathName/export.xlsx\")"
                2 + ".param(\"sort\", \"\")"
                +");"
            }

            method("template", JavaType.void) {
                annotation("@org.junit.jupiter.api.DisplayName(\"导入模板\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(1)")
                +"download(get(\"/$pathName/template.xlsx\")"
                +");"
            }

            method("importTemplate", JavaType.void) {
                annotation("@org.junit.jupiter.api.DisplayName(\"导入\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(1)")
                +"perform(multipart(\"/$pathName/import\")."
                2 + "file(file(\"file\", \"${remarks}导入数据.xlsx\"))"
                +");"
            }

        }.formattedContent
        println(code)
    }
}