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
            +"Page<$className> results = ${projectEntityName}Service.findAll(matcher, pageable);"
            if (isFullComposite) {
                +"return ok(results, $className::get$primaryKeyClassName);"
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

            import("org.springframework.http.MediaType")
            //saveForm
            import("javax.validation.groups.Default")
            method("saveForm", JavaType.objectInstance) {
                annotation("@top.bettercode.simpleframework.web.form.FormDuplicateCheck")
                annotation("@org.springframework.transaction.annotation.Transactional")
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, name = \"保存表单\")")
                parameter {
                    import("top.bettercode.simpleframework.web.validator.CreateConstraint")
                    annotation("@org.springframework.validation.annotation.Validated({Default.class, CreateConstraint.class})")
                    name = "form"
                    type = formType
                }
                +"return save(form);"
            }

            //saveBody
            import("javax.validation.groups.Default")
            method("saveBody", JavaType.objectInstance) {
                annotation("@top.bettercode.simpleframework.web.form.FormDuplicateCheck")
                annotation("@org.springframework.transaction.annotation.Transactional")
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", consumes = MediaType.APPLICATION_JSON_VALUE, name = \"保存JSON\")")
                parameter {
                    annotation("@org.springframework.web.bind.annotation.RequestBody")
                    import("top.bettercode.simpleframework.web.validator.CreateConstraint")
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
                import("org.springframework.util.StringUtils")
                +"if (${if (primaryKeyType == JavaType.stringInstance) "!StringUtils.hasText($primaryKeyName)" else "$primaryKeyName == null"}) {"
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
                +"${projectEntityName}Service.save($entityName);"
                +"return noContent();"
            }

            method("update", JavaType.objectInstance) {
                parameter {
                    name = "form"
                    type = formType
                }
                import("top.bettercode.lang.util.BeanUtil")
                +"${primaryKeyType.shortName} $primaryKeyName = form.get${primaryKeyName.capitalized()}();"
                +"$className exist = ${projectEntityName}Service.findById(${primaryKeyName}).orElseThrow(ResourceNotFoundException::new);"
                +"$className $entityName = ${if (isFullComposite) "new $className(form.getEntity())" else "form.getEntity()"};"
                +"BeanUtil.setNullPropertiesFrom($entityName, exist);"
                +"${projectEntityName}Service.save($entityName);"
                +"return noContent();"
            }

            method("delete", JavaType.objectInstance) {
                annotation("@top.bettercode.simpleframework.web.form.FormDuplicateCheck")
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
        method("form", formType) {
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
            +"return new ${formType.shortName}($entityName);"
        }

        //update
        import("org.springframework.http.MediaType")
        import("javax.validation.groups.Default")
        method("update", JavaType.objectInstance) {
            annotation("@top.bettercode.simpleframework.web.form.FormDuplicateCheck")
            annotation("@org.springframework.transaction.annotation.Transactional")
            annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/save\", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, params = \"${primaryKeyName}\", name = \"编辑\")")
            parameter {
                import("top.bettercode.simpleframework.web.validator.UpdateConstraint")
                annotation("@org.springframework.validation.annotation.Validated({Default.class, UpdateConstraint.class})")
                name = "form"
                type = formType
            }
            +"$className $entityName = ${if (isFullComposite) "new $className(form.getEntity())" else "form.getEntity()"};"
            +"${projectEntityName}Service.save($entityName);"
            +"return noContent();"
        }
    }
}
