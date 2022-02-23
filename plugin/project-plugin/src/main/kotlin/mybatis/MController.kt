import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Parameter

/**
 * @author Peter Wu
 */
open class MController : MProjectGenerator() {

    override fun content() {
        clazz(controllerType) {
            javadoc {
                +"/**"
                +" * $remarks 接口"
                +" */"
            }
            import(entityType)
            import("top.bettercode.simpleframework.exception.ResourceNotFoundException")

            superClass("$basePackageName.support.AppController")

            annotation("@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication")
            annotation("@org.springframework.validation.annotation.Validated")
            annotation("@org.springframework.web.bind.annotation.RestController")
            annotation("@org.springframework.web.bind.annotation.RequestMapping(value = \"/${table.subModule}/$pathName\", name = \"$remarks\")")

            val fieldType = serviceType
            field("${projectEntityName}Service", fieldType, isFinal = true)

            constructor(Parameter("${projectEntityName}Service", fieldType)) {
                +"this.${projectEntityName}Service = ${projectEntityName}Service;"
            }

            if (hasPrimaryKey) {
                //list
                val returnType = JavaType.objectInstance
                method("list", returnType) {
                    annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/list\", name = \"列表\")")
                    parameter {
                        type =
                            JavaType("com.baomidou.mybatisplus.plugins.Page").typeArgument(
                                entityType
                            )
                        name = "page"
                    }
                    parameter {
                        type = queryDslType
                        name = "wrapper"
                    }
                    +"Page<$className> results = ${projectEntityName}Service.selectPage(page, wrapper);"
                    +"return ok(results);"
                }

                val excel = enable("excel", false)
                if (excel) {
                    import("top.bettercode.lang.util.ArrayUtil")
                    field(
                        "excelFields",
                        JavaType("top.bettercode.util.excel.ExcelField<$className, ?>[]"),
                        isFinal = true
                    ) {
                        initializationString = "ArrayUtil.of(\n"
                        val size = columns.size
                        columns.forEachIndexed { i, it ->
                            val code =
                                if (it.isCodeField) {
                                    if (it.columnName.contains("_") || extension.softDeleteColumnName == it.columnName) ".code()" else ".code(${(className + it.javaName.capitalize())}Enum.ENUM_NAME)"
                                } else {
                                    ""
                                }
                            val propertyGetter =
                                if (it.isPrimary && isCompositePrimaryKey) "${it.javaType.shortNameWithoutTypeArguments}.class, from -> from.get${primaryKeyName.capitalize()}().get${it.javaName.capitalize()}()" else "$className::get${it.javaName.capitalize()}"
                            initializationString += "      ExcelField.of(\"${
                                it.remarks.split(
                                    Regex(
                                        "[:：,， (（]"
                                    )
                                )[0]
                            }\", $propertyGetter)${code}${if (i == size - 1) "" else ","}\n"
                        }

                        initializationString += "  );"
                    }
                    //export
                    method("export", JavaType.voidPrimitiveInstance) {
                        this.exception(JavaType("java.io.IOException"))
                        annotation("@top.bettercode.logging.annotation.RequestLogging(includeResponseBody = false, ignoredTimeout = true)")
                        annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/export.xlsx\", name = \"导出\")")
                        parameter {
                            type =
                                JavaType("com.baomidou.mybatisplus.plugins.Page").typeArgument(
                                    entityType
                                )
                            name = "page"
                        }
                        parameter {
                            type = queryDslType
                            name = "wrapper"
                        }

                        +"Page<$className> results = ${projectEntityName}Service.selectPage(page, wrapper);"
                        import("top.bettercode.util.excel.ExcelExport")
                        +"ExcelExport.export(request, response, \"$remarks\", excelExport -> excelExport.sheet(\"$remarks\").setData(results, excelFields));"
                    }
                }

                //info
                method("info", JavaType.objectInstance) {
                    annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/info\", name = \"详情\")")
                    parameter {
                        name = primaryKeyName
                        type = primaryKeyType
                        if (JavaType.stringInstance == type) {
                            annotation("@javax.validation.constraints.NotBlank")
                        } else {
                            annotation("@javax.validation.constraints.NotNull")
                        }
                    }
                    +"$className $entityName = ${projectEntityName}Service.selectById(${primaryKeyName});"
                    +"if ($entityName == null) {"
                    +"throw new ResourceNotFoundException();"
                    +"}"
                    +"return ok($entityName);"
                }

                //create
                import("javax.validation.groups.Default")
                method("create", JavaType.objectInstance) {
                    annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", params = \"!${primaryKeyName}\", name = \"新增\")")
                    parameter {
                        import("top.bettercode.simpleframework.web.validator.CreateConstraint")
                        annotation("@org.springframework.validation.annotation.Validated({Default.class, CreateConstraint.class})")
                        name = "${projectEntityName}Form"
                        type = formType
                    }
                    +"$className $entityName = ${projectEntityName}Form.getEntity();"
                    +"${projectEntityName}Service.insert($entityName);"
                    +"return noContent();"
                }

                //update
                method("update", JavaType.objectInstance) {
                    annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", params = \"${primaryKeyName}\", name = \"编辑\")")
                    parameter {
                        import("top.bettercode.simpleframework.web.validator.UpdateConstraint")
                        annotation("@org.springframework.validation.annotation.Validated({Default.class, UpdateConstraint.class})")
                        name = "${projectEntityName}Form"
                        type = formType
                    }
                    import("org.springframework.util.Assert")
                    +"$className $entityName = ${projectEntityName}Form.getEntity();"
                    +"${projectEntityName}Service.updateById($entityName);"
                    +"return noContent();"
                }

                //delete
                method("delete", JavaType.objectInstance) {
                    annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/delete\", name = \"删除\")")
                    parameter {
                        name = primaryKeyName
                        type = primaryKeyType
                        if (JavaType.stringInstance == type) {
                            annotation("@javax.validation.constraints.NotBlank")
                        } else {
                            annotation("@javax.validation.constraints.NotNull")
                        }
                    }
                    +"${projectEntityName}Service.deleteById(${primaryKeyName});"
                    +"return noContent();"
                }
            }
        }
    }
}