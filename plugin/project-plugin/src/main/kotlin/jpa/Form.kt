import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.InnerClass
import cn.bestwu.generator.dom.java.element.Parameter
import cn.bestwu.generator.dom.java.element.JavaVisibility

/**
 * @author Peter Wu
 */
class Form : ModuleJavaGenerator() {

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
            //constructor with id
            constructor(Parameter(primaryKeyName, primaryKeyType)) {
                +"this.entity = new $className(${primaryKeyName});"
            }
            //constructor with entity
            constructor(Parameter(entityName, entityType)) {
                +"this.entity = $entityName;"
            }

            method("getEntity", entityType) {
                +"return entity;"
            }

            import("javax.validation.groups.Default")

            //primaryKey getter
            method("get${primaryKeyName.capitalize()}", primaryKeyType) {
                javadoc {
                    +"/**"
                    +" * ${remarks}主键"
                    +" */"
                }
                import("cn.bestwu.simpleframework.web.validator.UpdateConstraint")
                if (primaryKeyType == JavaType.stringInstance) {
                    annotation("@javax.validation.constraints.NotBlank(groups = UpdateConstraint.class)")
                } else {
                    annotation("@javax.validation.constraints.NotNull(groups = UpdateConstraint.class)")
                }
                +"return this.entity.get${primaryKeyName.capitalize()}();"
            }

            otherColumns.forEach {
                //getter
                if (!it.jsonViewIgnored && it.javaName != "createdDate" && !it.isSoftDelete)
                    method("get${it.javaName.capitalize()}", it.javaType) {
                        if (it.columnSize > 0 && it.javaType == JavaType.stringInstance) {
                            annotation("@org.hibernate.validator.constraints.Length(max = ${it.columnSize}, groups = Default.class)")
                        }
                        if (!it.nullable) {
                            import("cn.bestwu.simpleframework.web.validator.CreateConstraint")
                            if (it.javaType == JavaType.stringInstance) {
                                annotation("@javax.validation.constraints.NotBlank(groups = CreateConstraint.class)")
                            } else {
                                annotation("@javax.validation.constraints.NotNull(groups = CreateConstraint.class)")
                            }
                        }
                        +"return this.entity.get${it.javaName.capitalize()}();"
                    }
            }
            //primaryKey setter
            method("set${primaryKeyName.capitalize()}") {
                javadoc {
                    +"/**"
                    +" * ${remarks}主键"
                    +" */"
                }
                parameter {
                    type = primaryKeyType
                    name = primaryKeyName
                }
                +"this.entity.set${primaryKeyName.capitalize()}(${primaryKeyName});"
            }
            otherColumns.forEach {
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