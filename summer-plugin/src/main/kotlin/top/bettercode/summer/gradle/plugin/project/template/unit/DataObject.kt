package top.bettercode.summer.gradle.plugin.project.template.unit

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.java.element.Parameter
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.lang.capitalized
import java.util.*

/**
 * @author Peter Wu
 */
//dataObject
val dataObject: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        import("java.util.Objects")

        javadoc {
            +"/** $remarks 对应数据库表名：$tableName */"
        }
        implement {
            +"java.io.Serializable"
        }
        serialVersionUID()

        if (defaultColumns.isNotEmpty()) {
            method(
                    "withDefaults",
                    entityType
            ) {
                isStatic = true
                javadoc {
                    +"/**"
                    +" * @return ${remarks}带默认值实例"
                    +" */"
                }
                +"$className $entityName = new $className();"
                defaultColumns.forEach {
                    +"$entityName.set${it.javaName.capitalized()}(${it.initializationString(this@apply)});"
                }
                +"return $entityName;"
            }

            method(
                    "nullWithDefaults",
                    entityType
            ) {
                javadoc {
                    +"/** 如果属性为null，设置默认值 */"
                }
                defaultColumns.forEach {
                    if (isFullComposite || isCompositePrimaryKey && it.isPrimary) {
                        +"if (this.${primaryKeyName}.get${it.javaName.capitalized()}() == null) {"
                        +"this.${primaryKeyName}.set${it.javaName.capitalized()}(${
                            it.initializationString(
                                    this@apply
                            )
                        });"
                        +"}"
                    } else {
                        +"if (this.${it.javaName} == null) {"
                        +"this.${it.javaName} = ${it.initializationString(this@apply)};"
                        +"}"
                    }
                }
                +"return this;"
            }
        }

        method(
                "nullFrom",
                entityType
        ) {
            javadoc {
                +"/**"
                +" * 如果属性为null，根据参数对象的属性设置值"
                +" *"
                +" * @param exist 已存在实体"
                +" */"
            }
            parameter(entityType, "exist")
            columns.forEach {
                if (isFullComposite || isCompositePrimaryKey && it.isPrimary) {
                    +"if (this.${primaryKeyName}.get${it.javaName.capitalized()}() == null) {"
                    +"this.${primaryKeyName}.set${it.javaName.capitalized()}(exist.get${it.javaName.capitalized()}());"
                    +"}"
                } else {
                    +"if (this.${it.javaName} == null) {"
                    +"this.${it.javaName} = exist.get${it.javaName.capitalized()}();"
                    +"}"
                }
            }
            +"return this;"
        }

        method(
                "from",
                entityType
        ) {
            javadoc {
                +"/** 从form表单对象更新实体属性 */"
            }
            parameter(JavaType.objectInstance, "form")
            import("org.springframework.beans.BeanUtils")
            +"BeanUtils.copyProperties(form, this);"
            +"return this;"
        }

        columns.forEach {
            //field
            field(it.javaName, it.javaType) {
                if (it.remark.isNotBlank() || it.columnDef != null)
                    javadoc {
                        +"/** ${it.docRemark} */"
                    }
            }

            //getter
            method(
                    "get${it.javaName.capitalized()}",
                    it.javaType
            ) {
                if (it.remark.isNotBlank() || it.columnDef != null)
                    javadoc {
                        +"/**"
                        +" * ${it.returnRemark}"
                        +" */"
                    }
                +"return ${it.javaName};"
            }
            //setter
            method(
                    "set${it.javaName.capitalized()}",
                    entityType,
                    Parameter(it.javaName, it.javaType)
            ) {
                javadoc {
                    +"/**"
                    +" * ${it.paramRemark}"
                    +" * @return ${remarks}实例"
                    +" */"
                }
                +"this.${it.javaName} = ${it.javaName};"
                +"return this;"
            }
        }

        //equals
        method(
                "equals",
                JavaType.boolean,
                Parameter("o", JavaType.objectInstance)
        ) {
            annotation("@Override")
            +"if (this == o) {"
            +"return true;"
            +"}"
            +"if (!(o instanceof ${className})) {"
            +"return false;"
            +"}"
            +"$className that = (${className}) o;"

            val size = otherColumns.size
            if (size == 0) {
                +"return Objects.equals(${primaryKeyName}, that.get${
                    primaryKeyName.capitalized()
                }());"
            } else {
                +"if (!Objects.equals(${primaryKeyName}, that.get${
                    primaryKeyName.capitalized()
                }())) {"
                +"return false;"
                +"}"
                if (size == 1) {
                    +"return Objects.equals(${otherColumns[0].javaName}, that.${otherColumns[0].javaName});"
                } else {
                    otherColumns.forEachIndexed { index, column ->
                        when (index) {
                            0 -> {
                                +"return Objects.equals(${column.javaName}, that.${column.javaName})"
                            }

                            size - 1 -> {
                                +"    && Objects.equals(${column.javaName}, that.${column.javaName});"
                            }

                            else -> {
                                +"    && Objects.equals(${column.javaName}, that.${column.javaName})"
                            }
                        }
                    }
                }
            }
        }

        //hashCode
        method("hashCode", JavaType.int) {
            annotation("@Override")
            if (isCompositePrimaryKey) {
                +"return Objects.hash(${
                    (listOf(primaryKeyName) + otherColumns.map { it.javaName }).joinToString(
                            ", "
                    )
                });"
            } else {
                +"return Objects.hash(${columns.joinToString(", ") { it.javaName }});"
            }
        }

        //toString
        import("top.bettercode.summer.tools.lang.util.StringUtil")
        method("toString", JavaType.stringInstance) {
            annotation("@Override")
            +"return StringUtil.json(this);"
        }
    }
}