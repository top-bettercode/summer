package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.lang.capitalized

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
            otherColumns.filter {
                !it.version && it.initializationString(this@apply, false).isNullOrBlank()
            }.forEach {
                +"$entityName.set${
                    it.javaName.capitalized()
                }(${it.randomValueToSet(this@apply)});"
            }
            if (defaultColumns.isNotEmpty()) {
                +"$entityName.nullPropertySetWithDefaults();"
            }
            +"${projectEntityName}Service.save($entityName);"
            +"System.err.println(\"------------------------------------------------------\");"
            +"return $entityName;"
        }
    }
}
