package jpa.unit

import ProjectGenerator
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.TopLevelClass
import top.bettercode.lang.capitalized

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
        method("beforeEach", JavaType.voidPrimitiveInstance) {
            annotation("@Override")
            exception(JavaType("Exception"))
            +"tableNames(${className}.TABLE_NAME);"
        }


        //list
        method("list", JavaType.voidPrimitiveInstance) {
//            javadoc {
//                +"// ${remarks}列表"
//            }
            annotation("@org.junit.jupiter.api.DisplayName(\"列表\")")
            annotation("@org.junit.jupiter.api.Test")
            annotation("@org.junit.jupiter.api.Order(0)")
            exception(JavaType("Exception"))
            +"$testInsertName();"
            +"perform(get(\"/$pathName/list\")"
            2 + ".param(\"page\", \"1\")"
            2 + ".param(\"size\", \"5\")"
            2 + ".param(\"sort\", \"\")"
            +");"
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
                +"$testInsertName();"
                +"download(get(\"/$pathName/export.xlsx\")"
                2 + ".param(\"sort\", \"\")"
                +");"
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
                +"${primaryKeyClassName} $primaryKeyName = $testInsertName().get${
                    primaryKeyName.capitalized()
                }();"
                +"perform(get(\"/$pathName/info\")"
                2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                +");"
            }

            val filterColumns = columns.filter { !it.testIgnored }
            val filterOtherColumns = otherColumns.filter { !it.testIgnored }

            import("org.springframework.http.MediaType")
            //saveForm
            method("saveForm", JavaType.voidPrimitiveInstance) {
//                javadoc {
//                    +"// ${remarks}保存表单"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"保存表单\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(3)")
                exception(JavaType("Exception"))
                if (isFullComposite) {
                    +"perform(post(\"/$pathName/save\")"
                    filterColumns.forEach {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                } else {
                    +"$primaryKeyClassName $primaryKeyName = $testInsertName().get${
                        primaryKeyName.capitalized()
                    }();"
                    +"perform(post(\"/$pathName/save\")"
                    2 + ".contentType(MediaType.APPLICATION_FORM_URLENCODED)"
                    2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                    filterOtherColumns.forEach {
                        2 + ".param(\"${it.javaName}\", \"${it.randomValue}\")"
                    }
                }
                +");"
            }

            //saveBody
            method("saveBody", JavaType.voidPrimitiveInstance) {
//                javadoc {
//                    +"// ${remarks}保存JSON"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"保存JSON\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(4)")
                exception(JavaType("Exception"))
                +"$className $entityName = new $className();"
                import(primaryKeyType)
                if (isCompositePrimaryKey) {
                    +"$primaryKeyClassName $primaryKeyName = new ${primaryKeyClassName}();"
                    primaryKeys.forEach {
                        +"$primaryKeyName.set${
                            it.javaName.capitalized()
                        }(${it.randomValueToSet(this@apply)});"
                    }
                    +"$entityName.set${
                        primaryKeyName.capitalized()
                    }(${primaryKeyName});"
                } else {
                    +"$primaryKeyClassName $primaryKeyName = $testInsertName().get${
                        primaryKeyName.capitalized()
                    }();"
                    primaryKeys.forEach {
                        +"$entityName.set${
                            it.javaName.capitalized()
                        }(${primaryKeyName});"
                    }
                }
                filterOtherColumns.forEach {
                    +"$entityName.set${
                        it.javaName.capitalized()
                    }(${it.randomValueToSet(this@apply)});"
                }
                +"perform(post(\"/$pathName/save\")"
                2 + ".contentType(MediaType.APPLICATION_JSON)"
                2 + ".content(json($entityName))"
                +");"
            }

            //delete
            staticImport("org.junit.jupiter.api.Assertions.assertFalse")
            method("delete", JavaType.voidPrimitiveInstance) {
//                javadoc {
//                    +"// ${remarks}删除"
//                }
                annotation("@org.junit.jupiter.api.DisplayName(\"删除\")")
                annotation("@org.junit.jupiter.api.Test")
                annotation("@org.junit.jupiter.api.Order(6)")
                exception(JavaType("Exception"))
                +"$primaryKeyClassName $primaryKeyName = $testInsertName().get${
                    primaryKeyName.capitalized()
                }();"
                +"perform(get(\"/$pathName/delete\")"
                2 + ".param(\"${primaryKeyName}\", String.valueOf(${primaryKeyName}))"
                +");"
                +"assertFalse(${projectEntityName}Service.existsById(${primaryKeyName}));"
            }


        }
    }
}

val testService: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        annotation("@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication")
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
            "insert${pathName.capitalized()}"

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
                            it.javaName.capitalized()
                        }(${it.randomValueToSet(this@apply)});"
                    }
                    +"$entityName.set${
                        primaryKeyName.capitalized()
                    }(${primaryKeyName});"
                } else
                    primaryKeys.forEach {
                        +"$entityName.set${
                            it.javaName.capitalized()
                        }(${it.randomValueToSet(this@apply)});"
                    }
            }
            otherColumns.filter { !it.version }.forEach {
                +"$entityName.set${
                    it.javaName.capitalized()
                }(${it.randomValueToSet(this@apply)});"
            }
            +"${projectEntityName}Service.save($entityName);"
            +"System.err.println(\"------------------------------------------------------\");"
            +"return $entityName;"
        }
    }
}
