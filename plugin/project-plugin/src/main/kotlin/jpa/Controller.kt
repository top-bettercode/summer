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
                    annotation("@org.springframework.data.web.PageableDefault${defaultSort()}")
                }
                import("org.springframework.data.domain.Page")
                +"Page<$className> results = ${projectEntityName}Service.findAll(${entityName}.spec(), pageable);"
                if (isFullComposite) {
                    import("org.springframework.data.domain.PageImpl")
                    import("java.util.stream.Collectors")
                    +"return ok(new PageImpl<>(results.getContent().stream().map($className::get$primaryKeyClass).collect(Collectors.toList()), pageable, results.getTotalElements()));"
                } else {
                    +"return ok(results);"
                }
            }

            val excel = enable("excel", false)
            if (excel) {
                import("top.bettercode.lang.util.ArrayUtil")
                field(
                    "excelFields",
                    JavaType("top.bettercode.util.excel.ExcelField<${if (isFullComposite) primaryKeyClass else className}, ?>[]"),
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
                            if (it.isPrimary && compositePrimaryKey) "${it.javaType.shortNameWithoutTypeArguments}.class, from -> from.get${primaryKeyName.capitalize()}().get${it.javaName.capitalize()}()" else "${if (isFullComposite) primaryKeyClass else className}::get${it.javaName.capitalize()}"
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
                    parameter {
                        type = JavaType("org.springframework.data.domain.Sort")
                        name = "sort"
                        annotation("@org.springframework.data.web.SortDefault${defaultSort()}")
                    }

                    +"Iterable<$className> results = ${projectEntityName}Service.findAll(${entityName}.spec(), sort);"
                    import("top.bettercode.util.excel.ExcelExport")
                    +"ExcelExport.export(request, response, \"$remarks\", excelExport -> excelExport.sheet(\"$remarks\").setData(${
                        if (isFullComposite) {
                            import("com.google.common.collect.Streams")
                            "Streams.stream(results).map($className::get$primaryKeyClass).collect(Collectors.toList())"
                        } else "results"
                    }, excelFields));"
                }
            }

            //info
            if (!isFullComposite)
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
                    import("java.util.Optional")
                    +"$className $entityName = ${projectEntityName}Service.findById(${primaryKeyName}).orElseThrow(ResourceNotFoundException::new);"
                    +"return ok($entityName${if (isFullComposite) "get${primaryKeyClass}()" else ""});"
                }

            if (isFullComposite) {
                //save
                import("javax.validation.groups.Default")
                method("create", JavaType.objectInstance) {
                    annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", name = \"新增\")")
                    parameter {
                        import("top.bettercode.simpleframework.web.validator.CreateConstraint")
                        annotation("@org.springframework.validation.annotation.Validated({Default.class, CreateConstraint.class})")
                        name = "${projectEntityName}Form"
                        type = formType
                    }
                    +"$className $entityName = ${if (isFullComposite) "new $className(${projectEntityName}Form.getEntity())" else "${projectEntityName}Form.getEntity()"};"
                    +"${projectEntityName}Service.save($entityName);"
                    +"return noContent();"
                }
            } else {
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
                    +"$className $entityName = ${if (isFullComposite) "new $className(${projectEntityName}Form.getEntity())" else "${projectEntityName}Form.getEntity()"};"
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
                    +"$className $entityName = ${if (isFullComposite) "new $className(${projectEntityName}Form.getEntity())" else "${projectEntityName}Form.getEntity()"};"
                    +"${projectEntityName}Service.dynamicSave($entityName);"
                    +"return noContent();"
                }
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

    //(sort = {AcAreaProperties.createdDate, AcAreaProperties.areaId}, direction = Direction.DESC)
    private fun TopLevelClass.defaultSort(): String {
        var sort = ""
        if (columns.any { it.javaName == "createdDate" } || !compositePrimaryKey) {
            import(propertiesType)
            import("org.springframework.data.domain.Sort.Direction")
            sort = "(sort = {"
            if (columns.any { it.javaName == "createdDate" }) {
                sort += "${className}Properties.createdDate"
                if (!compositePrimaryKey) {
                    sort += ", ${className}Properties.${primaryKeyName}"
                }
                sort += "}"
            } else if (!compositePrimaryKey) {
                sort += "${className}Properties.${primaryKeyName}}"
            }
            sort += ", direction = Direction.DESC)"
        }
        return sort
    }
}