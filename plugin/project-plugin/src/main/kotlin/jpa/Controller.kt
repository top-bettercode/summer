import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.Parameter

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
            import("cn.bestwu.simpleframework.exception.ResourceNotFoundException")

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
                    type = JavaType("com.querydsl.core.types.Predicate")
                    name = "predicate"
                    annotation("@org.springframework.data.querydsl.binding.QuerydslPredicate(root = $className.class)")
                }
                parameter {
                    type = JavaType("org.springframework.data.domain.Pageable")
                    name = "pageable"
                }
                import(queryDslType)
                val dateSort = if (columns.any { it.javaName == "createdDate" })
                    " Q${className}.${entityName}.createdDate.desc()," else ""
                import("org.springframework.data.domain.Page")
                val idSort =
                    if (compositePrimaryKey) "" else " Q${className}.${entityName}.${primaryKeyName}.desc()"
                val sep = if ((dateSort + idSort).isNotBlank()) "," else ""
                +"Page<$className> results = ${projectEntityName}Service.findAll(predicate, pageable${sep}${dateSort}$idSort);"
                +"return ok(results);"
            }

            val excel = enable("excel", false)
            if (excel) {
                import("cn.bestwu.lang.util.ArrayUtil")
                field(
                    "excelFields",
                    JavaType("cn.bestwu.util.excel.ExcelField<$className, ?>[]"),
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
                        initializationString += "      ExcelField.of(\"${it.remarks.split(Regex("[:：,， (（]"))[0]}\", $propertyGetter)${code}${if (i == size - 1) "" else ","}\n"
                    }

                    initializationString += "  );"
                }
                //export
                method("export", JavaType.voidPrimitiveInstance) {
                    this.exception(JavaType("java.io.IOException"))
                    annotation("@cn.bestwu.logging.annotation.RequestLogging(includeResponseBody = false, ignoredTimeout = true)")
                    annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/export.xlsx\", name = \"导出\")")
                    parameter {
                        type = JavaType("com.querydsl.core.types.Predicate")
                        name = "predicate"
                        annotation("@org.springframework.data.querydsl.binding.QuerydslPredicate(root = $className.class)")
                    }
                    val dateSort = if (columns.any { it.javaName == "createdDate" })
                        " Q${className}.${entityName}.createdDate.desc()," else ""
                    val idSort =
                        if (compositePrimaryKey) "" else " Q${className}.${entityName}.${primaryKeyName}.desc()"
                    val sep = if ((dateSort + idSort).isNotBlank()) "," else ""
                    +"Iterable<$className> results = ${projectEntityName}Service.findAll(predicate${sep}${dateSort}$idSort);"
                    import("cn.bestwu.util.excel.ExcelExport")
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
            import("javax.validation.groups.Default")
            //create
            method("create", JavaType.objectInstance) {
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/create\", name = \"新增\")")
                parameter {
                    import("cn.bestwu.simpleframework.web.validator.CreateConstraint")
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
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/update\", name = \"编辑\")")
                parameter {
                    import("cn.bestwu.simpleframework.web.validator.UpdateConstraint")
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