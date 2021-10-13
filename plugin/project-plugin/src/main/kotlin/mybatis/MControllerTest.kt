import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility

/**
 * @author Peter Wu
 */
open class MControllerTest : MModuleJavaGenerator() {

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
//            annotation("@org.junit.jupiter.api.DisplayName(\"${remarks}接口测试\")")
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

            if (hasPrimaryKey) {

                //insert
                val insertName = "insert${pathName.capitalize()}"
                method(insertName, entityType, visibility = JavaVisibility.PRIVATE) {
                    import(entityType)
                    import(JavaType("java.util.Random"))
                    import(JavaType("java.math.BigDecimal"))
                    import(JavaType("java.util.Date"))
                    import(JavaType("java.sql.Timestamp"))
                    import(JavaType("java.sql.Time"))
                    +"$className $entityName = new $className();"
                    columns.forEach {
                        if (!it.isPrimary && !it.jsonViewIgnored)
                            +"$entityName.set${it.javaName.capitalize()}(${it.randomValueToSet});"
                    }
                    +"${projectEntityName}Service.insert($entityName);"
                    +"return $entityName;"
                }

                //list
                method("list", JavaType.voidPrimitiveInstance) {
                    javadoc {
                        +"// ${remarks}列表"
                    }
//                annotation("@org.junit.jupiter.api.DisplayName(\"${remarks}列表\")")
                    annotation("@org.junit.jupiter.api.Test")
                    annotation("@org.junit.jupiter.api.Order(0)")
                    exception(JavaType("Exception"))
                    +"$insertName();"
                    +"mockMvc.perform(get(\"/$module/$pathName/list\")"
                    2 + ".param(\"page\", \"1\")"
                    2 + ".param(\"size\", \"5\")"
                    +").andExpect(status().isOk());"
                }

                //info
                method("info", JavaType.voidPrimitiveInstance) {
                    javadoc {
                        +"// ${remarks}详情"
                    }
//                annotation("@org.junit.jupiter.api.DisplayName(\"${remarks}详情\")")
                    annotation("@org.junit.jupiter.api.Test")
                    annotation("@org.junit.jupiter.api.Order(1)")
                    exception(JavaType("Exception"))
                    +"${primaryKeyType.shortName} $primaryKeyName = $insertName().get${primaryKeyName.capitalize()}();"
                    +"mockMvc.perform(get(\"/$module/$pathName/info\")"
                    2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                    +").andExpect(status().isOk());"
                }

                //create
                method("create", JavaType.voidPrimitiveInstance) {
                    javadoc {
                        +"// ${remarks}新增"
                    }
//                annotation("@org.junit.jupiter.api.DisplayName(\"${remarks}新增\")")
                    annotation("@org.junit.jupiter.api.Test")
                    annotation("@org.junit.jupiter.api.Order(2)")
                    exception(JavaType("Exception"))
                    +"mockMvc.perform(post(\"/$module/$pathName/save\")"
                    columns.forEach {
                        if (it.isPrimary) {
//                        2 + ".param(\"${it.javaName}\", \"1\")"
                        } else if (!it.isPrimary && !it.jsonViewIgnored) {
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
//                annotation("@org.junit.jupiter.api.DisplayName(\"${remarks}编辑\")")
                    annotation("@org.junit.jupiter.api.Test")
                    annotation("@org.junit.jupiter.api.Order(3)")
                    exception(JavaType("Exception"))
                    +"requires(\"${primaryKeyName}\");"
                    +"${primaryKeyType.shortName} $primaryKeyName = $insertName().get${primaryKeyName.capitalize()}();"
                    +"mockMvc.perform(post(\"/$module/$pathName/save\")"
                    2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                    columns.forEach {
                        if (!it.isPrimary && !it.jsonViewIgnored) {
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
//                annotation("@org.junit.jupiter.api.DisplayName(\"${remarks}删除\")")
                    annotation("@org.junit.jupiter.api.Test")
                    annotation("@org.junit.jupiter.api.Order(4)")
                    exception(JavaType("Exception"))
                    staticImport("org.junit.jupiter.api.Assertions.assertNull")
                    +"${primaryKeyType.shortName} $primaryKeyName = $insertName().get${primaryKeyName.capitalize()}();"
                    +"mockMvc.perform(post(\"/$module/$pathName/delete\")"
                    2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                    +").andExpect(status().isOk());"
                    +"assertNull(${projectEntityName}Service.selectById(${primaryKeyName}));"
                }
            }
        }
    }
}

