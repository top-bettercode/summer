package top.bettercode.summer.gradle.plugin.project.template.unit

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.lang.capitalized

/**
 * @author Peter Wu
 */
val controllerTest: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/** $remarks 控制层测试 */"
        }
        annotation("@org.junit.jupiter.api.DisplayName(\"${remarks}\")")
        annotation("@org.springframework.transaction.annotation.Transactional")
        superClass(baseWebTestType)

        import("org.junit.jupiter.api.MethodOrderer.OrderAnnotation")
        if (isCompositePrimaryKey)
            import(primaryKeyType)

        annotation("@org.junit.jupiter.api.TestMethodOrder(OrderAnnotation.class)")

        field("${projectEntityName}Service", if (interfaceService) iserviceType else serviceType) {
            annotation("@org.springframework.beans.factory.annotation.Autowired")
        }
        field("${projectEntityName}TestService", testServiceType) {
            annotation("@org.springframework.beans.factory.annotation.Autowired")
        }

        //beforeEach
        method("beforeEach", JavaType.void) {
            annotation("@Override")
            import(entityType)
            +"tableNames(${className}.TABLE_NAME);"
        }


        //list
        method("list", JavaType.void) {
//            javadoc {
//                +"// ${remarks}列表"
//            }
            annotation("@org.junit.jupiter.api.DisplayName(\"列表\")")
            annotation("@org.junit.jupiter.api.Test")
            annotation("@org.junit.jupiter.api.Order(0)")

            +"$testInsertName();"
            +""
            +"perform(get(\"/$pathName/list\")"
            2 + ".param(\"page\", \"1\")"
            2 + ".param(\"size\", \"5\")"
            2 + ".param(\"sort\", \"\")"
            +");"
        }

        val excel = enable("excel", false)
        if (excel) {
            //export
            method("export", JavaType.void) {
//                javadoc {
//                    +"// ${remarks}导出"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"导出\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(1)")

                +"$testInsertName();"
                +""
                +"download(get(\"/$pathName/export.xlsx\")"
                2 + ".param(\"sort\", \"\")"
                +");"
            }
        }

        if (!isFullComposite) {
            import("org.junit.jupiter.api.Assertions")
            //info
            method("info", JavaType.void) {
//                javadoc {
//                    +"// ${remarks}详情"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"详情\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(2)")

                +"$primaryKeyClassName $primaryKeyName = $testInsertName().get${
                    primaryKeyName.capitalized()
                }();"
                +"Assertions.assertNotNull($primaryKeyName);"
                +""
                +"perform(get(\"/$pathName/info\")"
                2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                +");"
            }

            val filterColumns = columns.filter { it.javaName != primaryKeyName && !it.testIgnored && (!it.isPrimary || isFullComposite) }
            val filterOtherColumns = otherColumns.filter { it.javaName != primaryKeyName && !it.testIgnored && (!it.isPrimary || isFullComposite) }

            import("org.springframework.http.MediaType")

            //saveBody
            method("saveBody", JavaType.void) {
                javadoc {
                    +"// 优先使用 ${remarks}保存(json)"
                }
                annotation("@org.junit.jupiter.api.DisplayName(\"保存(json)\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(3)")

                import(primaryKeyType)
                import(formType)
                if (isFullComposite) {
                    +"${formType.shortName} $entityName = new ${formType.shortName}();"
                    +""
                    filterColumns.forEach {
                        +"$entityName.set${
                            it.javaName.capitalized()
                        }(${it.randomValueToSet(this@apply)});"
                    }
                } else {
                    +"$primaryKeyClassName $primaryKeyName = $testInsertName().get${
                        primaryKeyName.capitalized()
                    }();"
                    +""
                    +"${formType.shortName} $entityName = new ${formType.shortName}();"
                    +"$entityName.set${
                        primaryKeyName.capitalized()
                    }(${primaryKeyName});"

                    filterOtherColumns.forEach {
                        +"$entityName.set${
                            it.javaName.capitalized()
                        }(${it.randomValueToSet(this@apply)});"
                    }
                }

                +""
                +"perform(post(\"/$pathName/save\")"
                2 + ".contentType(MediaType.APPLICATION_JSON)"
                2 + ".content(json($entityName))"
                +");"
            }

            //saveForm
            method("saveForm", JavaType.void) {
//                javadoc {
//                    +"// ${remarks}保存(form)"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"保存(form)\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(4)")

                if (isFullComposite) {
                    +"perform(post(\"/$pathName/save\")"
                    filterColumns.forEach {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                } else {
                    +"$primaryKeyClassName $primaryKeyName = $testInsertName().get${
                        primaryKeyName.capitalized()
                    }();"
                    +""
                    +"perform(post(\"/$pathName/save\")"
                    2 + ".contentType(MediaType.APPLICATION_FORM_URLENCODED)"
                    2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                    filterOtherColumns.forEach {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                }
                +");"
            }

            //delete
            method("delete", JavaType.void) {
//                javadoc {
//                    +"// ${remarks}删除"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"删除\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(6)")
                +"$primaryKeyClassName $primaryKeyName = $testInsertName().get${
                    primaryKeyName.capitalized()
                }();"
                +""
                +"perform(get(\"/$pathName/delete\")"
                2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                +");"
                +"Assertions.assertFalse(${projectEntityName}Service.existsById(${primaryKeyName}));"
            }


        }
    }
}

val baseWebTest: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        superClass = JavaType("top.bettercode.summer.test.BaseWebAuthTest")
    }
}


