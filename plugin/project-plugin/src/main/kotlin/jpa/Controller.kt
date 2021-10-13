import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Parameter
import top.bettercode.generator.dom.java.element.TopLevelClass

/**
 * @author Peter Wu
 */
open class Controller : ModuleJavaGenerator() {

    override val type: JavaType
        get() = controllerType

    override fun content() {

        clazz {
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
            annotation("@org.springframework.web.bind.annotation.RequestMapping(value = \"/$pathName\", name = \"$remarks\")")

            field("${projectEntityName}Service", serviceType, isFinal = true)

            constructor(Parameter("${projectEntityName}Service", serviceType)) {
                +"this.${projectEntityName}Service = ${projectEntityName}Service;"
            }
            if (hasPrimaryKey) {
                //list
                val returnType = JavaType.objectInstance
                method("list", returnType) {
                    annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/list\", name = \"列表\")")
                    parameter {
                        type = entityType
                        name = entityName
                    }
                    parameter {
                        type = JavaType("org.springframework.data.domain.Pageable")
                        name = "pageable"
                    }
                    import("org.springframework.data.domain.Example")
                    import("org.springframework.data.domain.Page")
                    +"Example<${className}> example = Example.of(${entityName}); "
                    +"Page<$className> results = ${projectEntityName}Service.findAll(example, pageable${sort()});"
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
                                if (it.isPrimary && compositePrimaryKey) "${it.javaType.shortNameWithoutTypeArguments}.class, from -> from.get${primaryKeyName.capitalize()}().get${it.javaName.capitalize()}()" else "$className::get${it.javaName.capitalize()}"
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
                            type = entityType
                            name = entityName
                        }

                        +"Example<${className}> example = Example.of(${entityName}); "
                        +"Iterable<$className> results = ${projectEntityName}Service.findAll(example${sort()});"
                        import("top.bettercode.util.excel.ExcelExport")
                        +"ExcelExport.export(request, response, \"$remarks\", excelExport -> excelExport.sheet(\"$remarks\").setData(results, excelFields));"
                    }
                }

                //info
                method("info", JavaType.objectInstance) {
                    annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/info\", name = \"详情\")")
                    parameter {
                        name = primaryKeyName
                        type = if (compositePrimaryKey) JavaType.stringInstance else primaryKeyType
                        if (JavaType.stringInstance == type) {
                            annotation("@javax.validation.constraints.NotBlank")
                        } else {
                            annotation("@javax.validation.constraints.NotNull")
                        }
                    }
                    import("java.util.Optional")
                    +"$className $entityName = ${projectEntityName}Service.findById(${if (compositePrimaryKey) "new ${primaryKeyType.shortNameWithoutTypeArguments}($primaryKeyName)" else primaryKeyName}).orElseThrow(ResourceNotFoundException::new);"
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
                    +"${projectEntityName}Service.save($entityName);"
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
                    +"$className $entityName = ${projectEntityName}Form.getEntity();"
                    +"${projectEntityName}Service.dynamicSave($entityName);"
                    +"return noContent();"
                }

                //delete
                method("delete", JavaType.objectInstance) {
                    annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/delete\", name = \"删除\")")
                    parameter {
                        name = primaryKeyName
                        type = if (compositePrimaryKey) JavaType.stringInstance else primaryKeyType
                        if (JavaType.stringInstance == type) {
                            annotation("@javax.validation.constraints.NotBlank")
                        } else {
                            annotation("@javax.validation.constraints.NotNull")
                        }
                    }
                    +"${projectEntityName}Service.deleteById(${if (compositePrimaryKey) "new ${primaryKeyType.shortNameWithoutTypeArguments}($primaryKeyName)" else primaryKeyName});"
                    +"return noContent();"
                }
            }
        }
    }

    private fun TopLevelClass.sort(): String {
        var sort = ""
        if (columns.any { it.javaName == "createdDate" } || !compositePrimaryKey) {
            import(propertiesType)
            import("org.springframework.data.domain.Sort.Direction")
            import("org.springframework.data.domain.Sort")
            sort = ", Sort.by(Direction.DESC, "
            if (columns.any { it.javaName == "createdDate" }) {
                sort += "P${className}.createdDate"
                if (!compositePrimaryKey) {
                    sort += ", P${className}.${primaryKeyName})"
                }
            } else if (!compositePrimaryKey) {
                sort += "P${className}.${primaryKeyName})"
            }
        }
        return sort
    }
}