package jpa.unit

import ProjectGenerator
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.TopLevelClass
import java.util.*

/**
 * @author Peter Wu
 */
val controllerTest: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * $remarks 控制层测试"
            +" */"
        }
        annotation("@org.junit.jupiter.api.DisplayName(\"${remarks}\")")
        annotation("@org.springframework.transaction.annotation.Transactional")
        superClass("$basePackageName.support.BaseWebTest")

        staticImport("org.junit.jupiter.api.Assertions.assertFalse")
        staticImport("org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get")
        staticImport("org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post")
        staticImport("org.springframework.test.web.servlet.result.MockMvcResultMatchers.status")

        annotation("@org.junit.jupiter.api.TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)")

        field("${projectEntityName}Service", if (interfaceService) iserviceType else serviceType) {
            annotation("@org.springframework.beans.factory.annotation.Autowired")
        }
        field("${projectEntityName}TestService", testServiceType) {
            annotation("@org.springframework.beans.factory.annotation.Autowired")
        }

        //setUp
        method("setUp", JavaType.voidPrimitiveInstance) {
            annotation("@org.junit.jupiter.api.BeforeEach")
//                exception(JavaType("Exception"))
            +"tableNames(\"$tableName\");"
        }

        val insertName =
            "${projectEntityName}TestService.insert${
                pathName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
            }"

        //list
        method("list", JavaType.voidPrimitiveInstance) {
//            javadoc {
//                +"// ${remarks}列表"
//            }
            annotation("@org.junit.jupiter.api.DisplayName(\"列表\")")
            annotation("@org.junit.jupiter.api.Test")
            annotation("@org.junit.jupiter.api.Order(0)")
            exception(JavaType("Exception"))
            +"$insertName();"
            +"mockMvc.perform(get(\"/$pathName/list\")"
            2 + ".param(\"page\", \"1\")"
            2 + ".param(\"size\", \"5\")"
            2 + ".param(\"sort\", \"\")"
            +").andExpect(status().isOk()).andExpect(contentStatusIsOk());"
        }

        val excel = enable("excel", false)
        if (excel) {
            //export
            method("export", JavaType.voidPrimitiveInstance) {
//                javadoc {
//                    +"// ${remarks}导出"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"导出\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(1)")
                exception(JavaType("Exception"))
                +"$insertName();"
                import("org.springframework.test.web.servlet.ResultActions")
                +"ResultActions actions = mockMvc.perform(get(\"/$pathName/export.xlsx\")"
                2 + ".param(\"sort\", \"\")"
                +");"
                +"download(actions);"
            }
        }

        if (!isFullComposite) {
            //info
            method("info", JavaType.voidPrimitiveInstance) {
//                javadoc {
//                    +"// ${remarks}详情"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"详情\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(2)")
                exception(JavaType("Exception"))
                +"${primaryKeyClassName} $primaryKeyName = $insertName().get${
                    primaryKeyName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }();"
                +"mockMvc.perform(get(\"/$pathName/info\")"
                2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                +").andExpect(status().isOk()).andExpect(contentStatusIsOk());"
            }

            val filterColumns =
                columns.filter { !it.version && !it.jsonViewIgnored && it.javaName != "createdDate" && it.javaName != "lastModifiedDate" }
            val filterOtherColumns =
                otherColumns.filter { !it.version && !it.jsonViewIgnored && it.javaName != "createdDate" && it.javaName != "lastModifiedDate" }
            //create
            method("create", JavaType.voidPrimitiveInstance) {
//                javadoc {
//                    +"// ${remarks}新增"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"新增\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(3)")
                exception(JavaType("Exception"))
                if (isCompositePrimaryKey && !isFullComposite) {
                    import(primaryKeyType)
                    +"${primaryKeyClassName} $primaryKeyName = new ${primaryKeyClassName}();"
                    primaryKeys.forEach {
                        +"$primaryKeyName.set${
                            it.javaName.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        }(${it.randomValueToSet});"
                    }
                }
                +"mockMvc.perform(post(\"/$pathName/save\")"
                if (isFullComposite) {
                    filterColumns.forEach {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                } else {
                    if (isCompositePrimaryKey || !primaryKey.autoIncrement) {
                        import(primaryKeyType)
                        if (isCompositePrimaryKey) {
                            2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                        } else {
                            2 + ".param(\"${primaryKeyName}\", \"${primaryKey.randomValue}\")"
                        }
                    }
                    filterOtherColumns.forEach {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                }
                +").andExpect(status().isOk()).andExpect(contentStatusIsOk());"
            }

            //update
            method("update", JavaType.voidPrimitiveInstance) {
//                javadoc {
//                    +"// ${remarks}编辑"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"编辑\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(4)")
                exception(JavaType("Exception"))
                if (isFullComposite) {
                    +"mockMvc.perform(post(\"/$pathName/save\")"
                    filterColumns.forEach {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                } else {
                    +"${primaryKeyClassName} $primaryKeyName = $insertName().get${
                        primaryKeyName.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        }
                    }();"
                    +"mockMvc.perform(post(\"/$pathName/save\")"
                    2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                    filterOtherColumns.forEach {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                }
                +").andExpect(status().isOk()).andExpect(contentStatusIsOk());"
            }

            //delete
            method("delete", JavaType.voidPrimitiveInstance) {
//                javadoc {
//                    +"// ${remarks}删除"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"删除\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(5)")
                exception(JavaType("Exception"))
                +"$primaryKeyClassName $primaryKeyName = $insertName().get${
                    primaryKeyName.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }
                }();"
                +"mockMvc.perform(post(\"/$pathName/delete\")"
                2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                +").andExpect(status().isOk()).andExpect(contentStatusIsOk());"
                +"assertFalse(${projectEntityName}Service.existsById(${primaryKeyName}));"
            }


        }
    }
}

val testService: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        annotation("@org.springframework.stereotype.Service")
        javadoc {
            +"/**"
            +" * $remarks 测试服务层"
            +" */"
        }
        field("${projectEntityName}Service", if (interfaceService) iserviceType else serviceType) {
            annotation("@org.springframework.beans.factory.annotation.Autowired")
        }

        val insertName =
            "insert${pathName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"

        //insert
        method(insertName, entityType, visibility = JavaVisibility.PUBLIC) {
            import(entityType)
            +"$className $entityName = new $className();"
            if (isCompositePrimaryKey || !primaryKey.autoIncrement) {
                import(primaryKeyType)
                if (isCompositePrimaryKey) {
                    +"$primaryKeyClassName $primaryKeyName = new ${primaryKeyClassName}();"
                    primaryKeys.forEach {
                        +"$primaryKeyName.set${
                            it.javaName.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        }(${it.randomValueToSet});"
                    }
                    +"$entityName.set${
                        primaryKeyName.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        }
                    }(${primaryKeyName});"
                } else
                    primaryKeys.forEach {
                        +"$entityName.set${
                            it.javaName.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        }(${it.randomValueToSet});"
                    }
            }
            otherColumns.filter { !it.version }.forEach {
                +"$entityName.set${
                    it.javaName.replaceFirstChar { it1 ->
                        if (it1.isLowerCase()) it1.titlecase(
                            Locale.getDefault()
                        ) else it1.toString()
                    }
                }(${it.randomValueToSet});"
            }
            +"${projectEntityName}Service.save($entityName);"
            +"System.err.println(\"-------------------------------\");"
            +"return $entityName;"
        }
    }
}
