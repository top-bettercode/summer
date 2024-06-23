package top.bettercode.summer.gradle.plugin.project.template.unit

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.generator.dom.java.element.Parameter
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.util.JavaType

/**
 * @author Peter Wu
 */
val controller: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/** $remarks 接口 */"
        }
        import(entityType)

        superClass(appControllerType)

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
            +"${matcherType.shortName} matcher = ${matcherType.shortName}.matching(${entityName});"
            +""
            +"Page<$className> results = ${projectEntityName}Service.findAll(matcher, pageable);"
            if (isFullComposite) {
                +"return ok(results, $className::get$primaryKeyClassName);"
            } else {
                +"return ok(results);"
            }
        }

        val excel = enable("excel", false)
        if (excel) {
            import("top.bettercode.summer.tools.lang.util.ArrayUtil")
            field(
                "excelFields",
                JavaType("top.bettercode.summer.tools.excel.ExcelField<${if (isFullComposite) primaryKeyClassName else className}, ?>[]"),
                isFinal = true
            ) {
                initializationString = "ArrayUtil.of(\n"
                val size = columns.size
                columns.forEachIndexed { i, it ->
                    val code =
                        if (it.isCodeField) {
                            if (it.javaName == it.codeType) ".code()" else ".code(${
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
                +""
                import("java.util.List")
                +"List<$className> results = ${projectEntityName}Service.findAll(matcher, sort);"
                import("top.bettercode.summer.tools.excel.ExcelExport")
                +"ExcelExport.sheet(\"$remarks\", excelExport -> excelExport.setData(${
                    if (isFullComposite) {
                        import("java.util.stream.Collectors")
                        "results.stream().map($className::get$primaryKeyClassName).collect(Collectors.toList())"
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

                +"$className $entityName = ${projectEntityName}Service.findById(${if (isCompositePrimaryKey) "${primaryKeyType.shortName}.of($primaryKeyName)" else primaryKeyName}).orElseThrow(notFound());"
                +""
                +"return ok($entityName${if (isFullComposite) "get${primaryKeyClassName}()" else ""});"
            }

            import("org.springframework.http.MediaType")

            //saveBody
            import("javax.validation.groups.Default")
            method("saveBody", JavaType.objectInstance) {
                annotation("@top.bettercode.summer.web.form.FormDuplicateCheck")
                annotation("@org.springframework.transaction.annotation.Transactional")
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", consumes = MediaType.APPLICATION_JSON_VALUE, name = \"保存(json)\")")
                parameter {
                    annotation("@org.springframework.web.bind.annotation.RequestBody")
                    import("top.bettercode.summer.web.validator.CreateConstraint")
                    annotation("@org.springframework.validation.annotation.Validated({Default.class, CreateConstraint.class})")
                    name = "form"
                    type = formType
                }
                +"return save(form);"
            }

            //saveForm
            import("javax.validation.groups.Default")
            method("saveForm", JavaType.objectInstance) {
                annotation("@top.bettercode.summer.web.form.FormDuplicateCheck")
                annotation("@org.springframework.transaction.annotation.Transactional")
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, name = \"保存(form)\")")
                parameter {
                    import("top.bettercode.summer.web.validator.CreateConstraint")
                    annotation("@org.springframework.validation.annotation.Validated({Default.class, CreateConstraint.class})")
                    name = "form"
                    type = formType
                }
                +"return save(form);"
            }

            method("save", JavaType.objectInstance) {
                parameter {
                    name = "form"
                    type = formType
                }
                +"${primaryKeyType.shortName} $primaryKeyName = form.get${primaryKeyName.capitalized()}();"

                +"if (${
                    if (primaryKeyType == JavaType.stringInstance) {
                        import("org.springframework.util.StringUtils")
                        "!StringUtils.hasText($primaryKeyName)"
                    } else "$primaryKeyName == null"
                }) {"
                +"return create(form);"
                +"} else {"
                +"return update(form);"
                +"}"
            }

            method("create", JavaType.objectInstance) {
                parameter {
                    name = "form"
                    type = formType
                }
                +"$className $entityName = ${if (isFullComposite) "new $className(form.getEntity())" else "form.getEntity()"};"
                +""
                if (defaultColumns.isNotEmpty())
                    +"$entityName.nullWithDefaults();"
                +"${projectEntityName}Service.save($entityName);"
                +"return noContent();"
            }

            method("update", JavaType.objectInstance) {
                parameter {
                    name = "form"
                    type = formType
                }
                +"${primaryKeyType.shortName} $primaryKeyName = form.get${primaryKeyName.capitalized()}();"
                +"$className $entityName = ${projectEntityName}Service.findById(${primaryKeyName}).orElseThrow(notFound());"
                +""
                +"$entityName.from(form);"
                +"${projectEntityName}Service.save($entityName);"
                +"return noContent();"
            }

            method("delete", JavaType.objectInstance) {
                annotation("@top.bettercode.summer.web.form.FormDuplicateCheck")
                annotation("@org.springframework.transaction.annotation.Transactional")
                annotation("@org.springframework.web.bind.annotation.RequestMapping(value = \"/delete\", name = \"删除\")")
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

val appController: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        superClass =
            JavaType("top.bettercode.summer.data.jpa.web.PageController")

        implement(serializationViewsType)
    }
}

