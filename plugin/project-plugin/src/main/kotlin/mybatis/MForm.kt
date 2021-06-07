import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Parameter

/**
 * @author Peter Wu
 */
class MForm : MModuleJavaGenerator() {

    override val type: JavaType
        get() = formType

    override fun content() {
        clazz {
            javadoc {
                +"/**"
                +" * $remarks 表单"
                +" */"
            }

            field(name = "entity", type = entityType, isFinal = true) {
                annotation("@com.fasterxml.jackson.annotation.JsonIgnore")
            }

            //constructor no args
            constructor {
                +"this.entity = new $className();"
            }
            //constructor with entity
            constructor(Parameter(entityName, entityType)) {
                +"this.entity = $entityName;"
            }

            method("getEntity", entityType) {
                +"return entity;"
            }

            import("javax.validation.groups.Default")

            columns.forEach {
                //getter
                if (it.isPrimary) {
                    import("top.bettercode.simpleframework.web.validator.UpdateConstraint")
                    annotation("@javax.validation.constraints.NotNull(groups = UpdateConstraint.class)")
                } else if (!it.jsonViewIgnored && it.javaName != "createdDate" && !it.isSoftDelete) {
                    method("get${it.javaName.capitalize()}", it.javaType) {
                        if (it.columnSize > 0 && it.javaType == JavaType.stringInstance) {
                            annotation("@org.hibernate.validator.constraints.Length(max = ${it.columnSize}, groups = Default.class)")
                        }
                        if (!it.nullable) {
                            import("top.bettercode.simpleframework.web.validator.CreateConstraint")
                            if (it.javaType == JavaType.stringInstance) {
                                annotation("@javax.validation.constraints.NotBlank(groups = CreateConstraint.class)")
                            } else {
                                annotation("@javax.validation.constraints.NotNull(groups = CreateConstraint.class)")
                            }
                        }
                        +"return this.entity.get${it.javaName.capitalize()}();"
                    }
                }
            }
            columns.forEach {
                //setter
                if (!it.jsonViewIgnored && it.javaName != "createdDate" && !it.isSoftDelete)
                    method("set${it.javaName.capitalize()}") {
                        parameter {
                            type = it.javaType
                            name = it.javaName
                        }
                        +"this.entity.set${it.javaName.capitalize()}(${it.javaName});"
                    }
            }
        }
    }

}
