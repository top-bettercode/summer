import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.Parameter

/**
 * @author Peter Wu
 */
open class MController : MModuleJavaGenerator() {

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
            annotation("@org.springframework.web.bind.annotation.RequestMapping(value = \"/$module/$pathName\", name = \"$moduleName\")")

            val fieldType = JavaType("$packageName.service.I${projectClassName}Service")
            field("${projectEntityName}Service", fieldType, isFinal = true)

            constructor(Parameter("${projectEntityName}Service", fieldType)) {
                +"this.${projectEntityName}Service = ${projectEntityName}Service;"
            }

            //list
            val returnType = JavaType.objectInstance
            method("list", returnType) {
                annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/list\", name = \"${remarks}列表\")")
                parameter {
                    type = JavaType("com.baomidou.mybatisplus.plugins.Page").typeArgument(entityType)
                    name = "page"
                }
                parameter {
                    type = JavaType("$packageName.querydsl.Q$className")
                    name = "wrapper"
                }
                +"Page<$className> results = ${projectEntityName}Service.selectPage(page, wrapper);"
                +"return ok(results);"
            }

            //info
            method("info", JavaType.objectInstance) {
                annotation("@org.springframework.web.bind.annotation.GetMapping(value = \"/info\", name = \"${remarks}详情\")")
                parameter {
                    annotation("@javax.validation.constraints.NotNull")
                    name = primaryKeyName
                    type = primaryKeyType
                }
                +"$className $entityName = ${projectEntityName}Service.selectById(${primaryKeyName});"
                +"if ($entityName == null) {"
                +"throw new ResourceNotFoundException();"
                +"}"
                +"return ok($entityName);"
            }

            import("javax.validation.groups.Default")
            //create
            method("create", JavaType.objectInstance) {
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/create\", name = \"${remarks}新增\")")
                parameter {
                    import("cn.bestwu.simpleframework.web.validator.CreateConstraint")
                    annotation("@org.springframework.validation.annotation.Validated({Default.class, CreateConstraint.class})")
                    name = "${projectEntityName}Form"
                    type = JavaType("$packageName.form.${projectClassName}Form")
                }
                +"$className $entityName = ${projectEntityName}Form.getEntity();"
                +"${projectEntityName}Service.insert($entityName);"
                +"return ok($entityName);"
            }

            //update
            method("update", JavaType.objectInstance) {
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/update\", name = \"${remarks}编辑\")")
                parameter {
                    import("cn.bestwu.simpleframework.web.validator.UpdateConstraint")
                    annotation("@cn.bestwu.simpleframework.web.resolver.ModifyModel${if (primaryKeyName != "id") "(value = $className.class, idParameter = \"${primaryKeyName}\")" else "($className.class)"} @org.springframework.validation.annotation.Validated({Default.class, UpdateConstraint.class})")
                    name = "${projectEntityName}Form"
                    type = JavaType("$packageName.form.${projectClassName}Form")
                }
                import("org.springframework.util.Assert")
                +"$className $entityName = ${projectEntityName}Form.getEntity();"
                +"${projectEntityName}Service.updateById($entityName);"
                +"return ok($entityName);"
            }

            //delete
            method("delete", JavaType.objectInstance) {
                annotation("@org.springframework.web.bind.annotation.PostMapping(value = \"/delete\", name = \"${remarks}删除\")")
                parameter {
                    annotation("@javax.validation.constraints.NotNull")
                    name = primaryKeyName
                    type = primaryKeyType
                }
                +"${projectEntityName}Service.deleteById(${primaryKeyName});"
                +"return noContent();"
            }
        }
    }
}