import org.gradle.configurationcache.extensions.capitalized
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Parameter
import top.bettercode.generator.dom.java.element.TopLevelClass

/**
 * @author Peter Wu
 */
val controller: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * $remarks 接口"
            +" */"
        }
        import(entityType)

        superClass("$basePackageName.support.${projectName.capitalized()}Controller")

        annotation("@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication")
        annotation("@org.springframework.validation.annotation.Validated")
        annotation("@org.springframework.web.bind.annotation.RestController")
        annotation("@org.springframework.web.bind.annotation.RequestMapping(value = \"/$pathName\", name = \"$remarks\")")

        field(
            "${projectEntityName}Service",
            if (interfaceService) iserviceType else serviceType,
            isFinal = true
        )

        constructor(
            Parameter(
                "${projectEntityName}Service",
                if (interfaceService) iserviceType else serviceType
            )
        ) {
            +"this.${projectEntityName}Service = ${projectEntityName}Service;"
        }

        var defaultSort = ""
        if (columns.any { it.javaName == "createdDate" } || !isCompositePrimaryKey) {
            import(propertiesType)
            import("org.springframework.data.domain.Sort.Direction")
            defaultSort = "(sort = {"
            if (columns.any { it.javaName == "createdDate" }) {
                defaultSort += "${propertiesType.shortName}.createdDate"
                if (!isCompositePrimaryKey) {
                    defaultSort += ", ${propertiesType.shortName}.${primaryKeyName}"
                }
                defaultSort += "}"
            } else if (!isCompositePrimaryKey) {
                defaultSort += "${propertiesType.shortName}.${primaryKeyName}}"
            }
            defaultSort += ", direction = Direction.DESC)"
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
                annotation("@org.springframework.data.web.PageableDefault${defaultSort}")
            }
            import("org.springframework.data.domain.Page")
            import(matcherType)
            +"Page<$className> results = ${projectEntityName}Service.findAll(${matcherType.shortName}.matching(${entityName}), pageable);"
            if (isFullComposite) {
                import("org.springframework.data.domain.PageImpl")
                import("java.util.stream.Collectors")
                +"return ok(new PageImpl<>(results.getContent().stream().map($className::get$primaryKeyClassName).collect(Collectors.toList()), pageable, results.getTotalElements()));"
            } else {
                +"return ok(results);"
            }
        }

        val excel = enable("excel", false)
        if (excel) {
            import("top.bettercode.lang.util.ArrayUtil")
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
                        if (it.isPrimary && isCompositePrimaryKey) "${it.javaType.shortNameWithoutTypeArguments}.class, from -> from.get${
                            primaryKeyName.capitalized()
                        }().get${
                            it.javaName.capitalized()
                        }()" else "${if (isFullComposite) primaryKeyClassName else className}::get${
                            it.javaName.capitalized()
                        }"
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
                annotation("@top.bettercode.logging.annotation.RequestLogging(includeResponseBody = false, ignoredTimeout = true)")
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

                +"Iterable<$className> results = ${projectEntityName}Service.findAll(${matcherType.shortName}.matching(${entityName}), sort);"
                import("top.bettercode.util.excel.ExcelExport")
                +"ExcelExport.sheet(\"$remarks\", excelExport -> excelExport.setData(${
                    if (isFullComposite) {
                        import("com.google.common.collect.Streams")
                        "Streams.stream(results).map($className::get$primaryKeyClassName).collect(Collectors.toList())"
                    } else "results"
                }, excelFields));"
            }
        }

        if (!isFullComposite) {

            //info
            method("info", JavaType.objectInstance) {
                annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/info\", name = \"详情\")")
                parameter {
                    name = primaryKeyName
                    type =
                        if (isCompositePrimaryKey) JavaType.stringInstance else primaryKeyType
                    if (JavaType.stringInstance == type) {
                        annotation("@javax.validation.constraints.NotBlank")
                    } else {
                        annotation("@javax.validation.constraints.NotNull")
                    }
                }
                import("top.bettercode.simpleframework.exception.ResourceNotFoundException")

                +"$className $entityName = ${projectEntityName}Service.findById(${if (isCompositePrimaryKey) "${primaryKeyType.shortName}.of($primaryKeyName)" else primaryKeyName}).orElseThrow(ResourceNotFoundException::new);"
                +"return ok($entityName${if (isFullComposite) "get${primaryKeyClassName}()" else ""});"
            }

            //create
            import("javax.validation.groups.Default")
            method("create", JavaType.objectInstance) {
                annotation("@top.bettercode.simpleframework.web.form.FormDuplicateCheck")
                annotation("@org.springframework.transaction.annotation.Transactional")
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", params = \"!${primaryKeyName}\", name = \"新增\")")
                parameter {
                    import("top.bettercode.simpleframework.web.validator.CreateConstraint")
                    annotation("@org.springframework.validation.annotation.Validated({Default.class, CreateConstraint.class})")
                    name = "${entityName}Form"
                    type = formType
                }
                +"$className $entityName = ${if (isFullComposite) "new $className(${entityName}Form.getEntity())" else "${entityName}Form.getEntity()"};"
                +"${projectEntityName}Service.save($entityName);"
                +"return noContent();"
            }

            method("delete", JavaType.objectInstance) {
                annotation("@top.bettercode.simpleframework.web.form.FormDuplicateCheck")
                annotation("@org.springframework.transaction.annotation.Transactional")
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/delete\", name = \"删除\")")
                parameter {
                    name = primaryKeyName
                    type =
                        if (isCompositePrimaryKey) JavaType.stringInstance else primaryKeyType
                    if (JavaType.stringInstance == type) {
                        annotation("@javax.validation.constraints.NotBlank")
                    } else {
                        annotation("@javax.validation.constraints.NotNull")
                    }
                }
                +"${projectEntityName}Service.deleteById(${if (isCompositePrimaryKey) "${primaryKeyType.shortName}.of($primaryKeyName)" else primaryKeyName});"
                +"return noContent();"
            }
        }
    }

}

val updateController: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * $remarks 接口"
            +" */"
        }
        import(entityType)

        superClass("$basePackageName.support.${projectName.capitalized()}Controller")

        annotation("@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication")
        annotation("@org.springframework.validation.annotation.Validated")
        annotation("@org.springframework.web.bind.annotation.RestController")
        annotation("@org.springframework.web.bind.annotation.RequestMapping(value = \"/$pathName\", name = \"$remarks\")")

        field(
            "${projectEntityName}Service",
            if (interfaceService) iserviceType else serviceType,
            isFinal = true
        )

        constructor(
            Parameter(
                "${projectEntityName}Service",
                if (interfaceService) iserviceType else serviceType
            )
        ) {
            +"this.${projectEntityName}Service = ${projectEntityName}Service;"
        }

        import("top.bettercode.simpleframework.exception.ResourceNotFoundException")
        method("${entityName}Form", formType) {
            annotation("@org.springframework.web.bind.annotation.ModelAttribute")
            parameter {
                name = primaryKeyName
                type =
                    if (isCompositePrimaryKey) JavaType.stringInstance else primaryKeyType
                if (JavaType.stringInstance == type) {
                    annotation("@javax.validation.constraints.NotBlank")
                } else {
                    annotation("@javax.validation.constraints.NotNull")
                }
            }
            +"$className $entityName = ${projectEntityName}Service.findById(${if (isCompositePrimaryKey) "${primaryKeyType.shortName}.of($primaryKeyName)" else primaryKeyName}).orElseThrow(ResourceNotFoundException::new);"
            +"return new ${projectClassName}Form($entityName);"
        }

        //update
        import("javax.validation.groups.Default")
        method("update", JavaType.objectInstance) {
            annotation("@top.bettercode.simpleframework.web.form.FormDuplicateCheck")
            annotation("@org.springframework.transaction.annotation.Transactional")
            annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", params = \"${primaryKeyName}\", name = \"编辑\")")
            parameter {
                import("top.bettercode.simpleframework.web.validator.UpdateConstraint")
                annotation("@org.springframework.validation.annotation.Validated({Default.class, UpdateConstraint.class})")
                name = "${entityName}Form"
                type = formType
            }
            +"$className $entityName = ${if (isFullComposite) "new $className(${entityName}Form.getEntity())" else "${entityName}Form.getEntity()"};"
            +"${projectEntityName}Service.save($entityName);"
            +"return noContent();"
        }
    }
}
