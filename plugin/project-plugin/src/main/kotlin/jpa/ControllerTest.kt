import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.JavaVisibility

/**
 * @author Peter Wu
 */
open class ControllerTest : ModuleJavaGenerator() {

    override val test: Boolean = true
    override val type: JavaType
        get() = controllerTestType

    override fun content() {

        clazz {
            javadoc {
                +"/**"
                +" * $remarks 控制层测试"
                +" */"
            }
//            annotation("@org.junit.jupiter.api.DisplayName(\"${remarks}\")")
            annotation("@org.springframework.transaction.annotation.Transactional")
            superClass("$basePackageName.support.BaseWebTest")

            staticImport("org.junit.jupiter.api.Assertions.assertFalse")
            staticImport("org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get")
            staticImport("org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post")
            staticImport("org.springframework.test.web.servlet.result.MockMvcResultMatchers.status")

            annotation("@org.junit.jupiter.api.TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)")
            val tableName =
                if (schema.isNullOrBlank() || schema == extension.datasource.schema) tableName else "$schema.$tableName"

            field("${projectEntityName}Service", serviceType) {
                annotation("@org.springframework.beans.factory.annotation.Autowired")
            }

            //setUp
            method("setUp", JavaType.voidPrimitiveInstance) {
                annotation("@org.junit.jupiter.api.BeforeEach")
//                exception(JavaType("Exception"))
                +"tableNames(\"$tableName\");"
            }
            val insertName = "insert${pathName.capitalize()}"

            //insert
            method(insertName, entityType, visibility = JavaVisibility.PRIVATE) {
                import(entityType)
                import(JavaType("java.util.Random"))
                import(JavaType("java.math.BigDecimal"))
                import(JavaType("java.util.Date"))
                import(JavaType("java.sql.Timestamp"))
                import(JavaType("java.sql.Time"))
                +"$className $entityName = new $className();"
                if (compositePrimaryKey || !primaryKey.autoIncrement) {
                    import(primaryKeyType)
                    if (compositePrimaryKey) {
                        +"${primaryKeyType.shortName} $primaryKeyName = new ${primaryKeyType.shortName}();"
                        primaryKeys.forEach {
                            +"$primaryKeyName.set${it.javaName.capitalize()}(${it.randomValueToSet});"
                        }
                        +"$entityName.set${primaryKeyName.capitalize()}(${primaryKeyName});"
                    } else
                        primaryKeys.forEach {
                            +"$entityName.set${it.javaName.capitalize()}(${it.randomValueToSet});"
                        }
                }
                otherColumns.forEach {
//                    if (!it.jsonViewIgnored)
                        +"$entityName.set${it.javaName.capitalize()}(${it.randomValueToSet});"
                }
                +"${projectEntityName}Service.save($entityName);"
                +"return $entityName;"
            }

            //list
            method("list", JavaType.voidPrimitiveInstance) {
                javadoc {
                    +"// ${remarks}列表"
                }
//                annotation("@org.junit.jupiter.api.DisplayName(\"列表\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(0)")
                exception(JavaType("Exception"))
                +"$insertName();"
                +"mockMvc.perform(get(\"/$pathName/list\")"
                2 + ".param(\"page\", \"1\")"
                2 + ".param(\"size\", \"5\")"
                +").andExpect(status().isOk());"
            }

            val excel = enable("excel", false)
            if (excel) {
                //export
                method("export", JavaType.voidPrimitiveInstance) {
                    javadoc {
                        +"// ${remarks}导出"
                    }
//                annotation("@org.junit.jupiter.api.DisplayName(\"导出\")")
                    annotation("@org.junit.jupiter.api.Test")
                    annotation("@org.junit.jupiter.api.Order(1)")
                    exception(JavaType("Exception"))
                    +"$insertName();"
                    import("org.springframework.test.web.servlet.ResultActions")
                    +"ResultActions actions = mockMvc.perform(get(\"/$pathName/export.xlsx\")"
                    +");"
                    +"download(actions);"
                }
            }

            //info
            method("info", JavaType.voidPrimitiveInstance) {
                javadoc {
                    +"// ${remarks}详情"
                }
//                annotation("@org.junit.jupiter.api.DisplayName(\"详情\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(2)")
                exception(JavaType("Exception"))
                +"${primaryKeyType.shortName} $primaryKeyName = $insertName().get${primaryKeyName.capitalize()}();"
                +"mockMvc.perform(get(\"/$pathName/info\")"
                2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                +").andExpect(status().isOk());"
            }

            //create
            method("create", JavaType.voidPrimitiveInstance) {
                javadoc {
                    +"// ${remarks}新增"
                }
//                annotation("@org.junit.jupiter.api.DisplayName(\"新增\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(3)")
                exception(JavaType("Exception"))
                if (compositePrimaryKey || !primaryKey.autoIncrement) {
                    import(primaryKeyType)
                    if (compositePrimaryKey) {
                        +"${primaryKeyType.shortName} $primaryKeyName = new ${primaryKeyType.shortName}();"
                        primaryKeys.forEach {
                            +"$primaryKeyName.set${it.javaName.capitalize()}(${it.randomValueToSet});"
                        }
                    }
                }
                +"mockMvc.perform(post(\"/$pathName/create\")"
                if (compositePrimaryKey || !primaryKey.autoIncrement) {
                    import(primaryKeyType)
                    if (compositePrimaryKey) {
                        2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                    } else {
                        2 + ".param(\"${primaryKeyName}\", \"${primaryKey.randomValue}\")"
                    }
                }
                otherColumns.forEach {
                    if (!it.jsonViewIgnored && it.javaName != "createdDate") {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                }
                +").andExpect(status().isOk());"
            }

            //update
            method("update", JavaType.voidPrimitiveInstance) {
                javadoc {
                    +"// ${remarks}编辑"
                }
//                annotation("@org.junit.jupiter.api.DisplayName(\"编辑\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(4)")
                exception(JavaType("Exception"))
                +"requires(\"${primaryKeyName}\");"
                +"${primaryKeyType.shortName} $primaryKeyName = $insertName().get${primaryKeyName.capitalize()}();"
                +"mockMvc.perform(post(\"/$pathName/update\")"
                2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                columns.forEach {
                    if (!it.isPrimary && !it.jsonViewIgnored && it.javaName != "createdDate") {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                }
                +").andExpect(status().isOk());"
            }

            //delete
            method("delete", JavaType.voidPrimitiveInstance) {
                javadoc {
                    +"// ${remarks}删除"
                }
//                annotation("@org.junit.jupiter.api.DisplayName(\"删除\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(5)")
                exception(JavaType("Exception"))
                +"${primaryKeyType.shortName} $primaryKeyName = $insertName().get${primaryKeyName.capitalize()}();"
                +"mockMvc.perform(post(\"/$pathName/delete\")"
                2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                +").andExpect(status().isOk());"
                +"assertFalse(${projectEntityName}Service.findById(${primaryKeyName}).isPresent());"
            }
        }
    }
}

