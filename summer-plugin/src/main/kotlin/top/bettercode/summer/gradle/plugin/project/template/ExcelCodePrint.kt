package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.util.JavaType

/**
 * @author Peter Wu
 */
open class ExcelCodePrint : ProjectGenerator() {

    override fun call() {
        val code = clazz(controllerType) {

            val excelClassName = className

            val filterColumns =
                columns.filter { it.javaName != primaryKeyName && !it.testIgnored && (!it.isPrimary || isFullComposite) }

            field(
                "rowSetter",
                JavaType("top.bettercode.summer.tools.excel.write.RowSetter<${if (isFullComposite) primaryKeyClassName else className}>"),
                isFinal = true
            ) {
                initializationString = "RowSetter.of(\n"
                val size = filterColumns.size
                filterColumns.forEachIndexed { i, it ->
                    val code =
                        if (it.isCodeField) {
                            if (it.javaName == it.codeType) ".code()" else ".code(${
                                it.codeType.capitalized()
                            }Enum.ENUM_NAME).dataValidation(${
                                it.dicCodes()!!.codes.values.joinToString(
                                    ", "
                                ) { s -> "\"$s\"" }
                            })"
                        } else {
                            ""
                        }
                    import("top.bettercode.summer.tools.excel.write.CellSetter")
                    val propertyGetter =
                        "${excelClassName}::get${it.javaName.capitalized()}"
                    initializationString += "      CellSetter.of(\"${
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
                +"ExcelWriter.sheet(\"$remarks\", writer -> writer.setData(${
                    if (isFullComposite) {
                        "results.stream().map($className::get$primaryKeyClassName).collect(Collectors.toList())"
                    } else "results"
                }, rowSetter));"
            }

            //template
            method("template", JavaType.void) {
                annotation("@top.bettercode.summer.logging.annotation.RequestLogging(includeResponseBody = false, ignoredTimeout = true)")
                annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/template.xlsx\", name = \"导入模板\")")

                import("top.bettercode.util.excel.ExcelExport")
                +"ExcelWriter.sheet(\"${remarks}导入模板\", writer -> writer.template(rowSetter));"
            }

            //import
            method("importTemplate", JavaType.objectInstance) {
                annotation("@org.springframework.transaction.annotation.Transactional")
                annotation("@top.bettercode.summer.logging.annotation.RequestLogging(includeRequestBody = false, ignoredTimeout = true)")
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/import\", name = \"导入\")")
                parameter {
                    type = JavaType("org.springframework.web.multipart.MultipartFile")
                    name = "file"
                }
                +"try(ExcelReader reader = ExcelReader.of(file)){"
                2 + "List<${formType.shortName}> list = reader.validateGroups(Default.class, CreateConstraint.class).getData(rowSetter.toGetter());"
                2 + "for (${formType.shortName} form : list) {"
                2 + "create(form);"
                2 + "}"
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