package cn.bestwu.generator.dsl.def

import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.Parameter
import cn.bestwu.generator.dsl.JavaGenerator

/**
 * @author Peter Wu
 */
class Domain : JavaGenerator() {

    override var cover: Boolean = true
    override val name: String
        get() = "domain.$className"


    override fun content() {
        clazz {
            javadoc {
                +"/**"
                +" * $remarks 对应表名：$tableName"
                +" */"
            }
            implement {
                +"java.io.Serializable"
            }
            serialVersionUID()
            columns.forEach {
                //field
                field(it.javaName, it.javaType) {
                    if (it.remarks.isNotBlank())
                        if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                            javadoc {
                                +"/**"
                                +" * ${it.remarks}${it.defaultRemarks}"
                                +" */"
                            }
                    if (!it.isPrimary) {
                        if (it.columnSize > 0 && it.javaType == JavaType.stringInstance) {
                            annotation("@org.hibernate.validator.constraints.Length(max = ${it.columnSize})")
                        }
                        if (!it.nullable) {
                            if (it.javaType == JavaType.stringInstance) {
                                annotation("@org.hibernate.validator.constraints.NotBlank")
                            } else {
                                annotation("@javax.validation.constraints.NotNull")
                            }
                        }
                    }
                    if (!it.columnDef.isNullOrBlank())
                        initializationString = it.initializationString
                }
            }

            //constructor no args
            constructor {}
            //constructor with id
            constructor(Parameter(primaryKey.javaName, primaryKey.javaType)) {
                +"this.${primaryKey.javaName} = ${primaryKey.javaName};"
            }

            columns.forEach {
                //getter
                method("get${it.javaName.capitalize()}", it.javaType) {
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * @return ${(if (it.remarks.isBlank()) "" else it.remarks)}${it.defaultRemarks}"
                            +" */"
                        }
                    +"return ${it.javaName};"
                }
                //setter
                method("set${it.javaName.capitalize()}") {
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * @param ${if (it.remarks.isBlank()) "" else "${it.javaName} ${it.remarks}"}${it.defaultRemarks}"
                            +" */"
                        }
                    parameter {
                        type = it.javaType
                        name = it.javaName
                    }
                    +"this.${it.javaName} = ${it.javaName};"
                }
            }
        }
    }
}