package jpa.unit

import ProjectGenerator
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.Parameter
import top.bettercode.generator.dom.java.element.TopLevelClass
import top.bettercode.lang.capitalized

/**
 *
 * @author Peter Wu
 */
val compositePrimaryKey: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        import("java.util.Objects")
        import("javax.persistence.Embeddable")
        visibility = JavaVisibility.PUBLIC
        annotation("@javax.persistence.Embeddable")
        javadoc {
            +"/**"
            +" * $remarks 主键 对应表名：$tableName"
            +" */"
        }
        implement {
            +"java.io.Serializable"
        }
        serialVersionUID()

        if (isFullComposite)
            field("TABLE_NAME", JavaType.stringInstance, "\"${tableName}\"") {
                visibility = JavaVisibility.PUBLIC
                isStatic = true
                isFinal = true
                javadoc {
                    +"/**"
                    +" * 对应数据库表名"
                    +" */"
                }
            }

        //constructor no args
        constructor { }

        constructor {
            primaryKeys.forEach { column ->
                parameter(column.javaType, column.javaName)
                +"this.${column.javaName} = ${column.javaName};"
            }
        }

        if (!isFullComposite) {
            import("top.bettercode.simpleframework.support.EmbeddedIdConverter")
            method("of", type, Parameter(primaryKeyName, JavaType.stringInstance)) {
                isStatic = true
                +"return EmbeddedIdConverter.toEmbeddedId($primaryKeyName, ${type.shortName}.class);"
            }
            method(
                "of",
                type,
                Parameter(primaryKeyName, JavaType.stringInstance),
                Parameter("delimiter", JavaType.stringInstance)
            ) {
                isStatic = true
                +"return EmbeddedIdConverter.toEmbeddedId($primaryKeyName, delimiter, ${type.shortName}.class);"
            }
        }

        primaryKeys.forEach {
            //field
            field(it.javaName, it.javaType) {
                if (it.remark.isNotBlank() || it.columnDef != null)
                    javadoc {
                        +"/**"
                        +" * ${it.docRemark}"
                        +" */"
                    }

                var columnAnnotation =
                    "@javax.persistence.Column(name = \"${it.columnName}\", columnDefinition = \"${it.typeDesc}${it.defaultDesc}${if (it.extra.isBlank()) "" else " ${it.extra}"}\""
                if (it.columnSize > 0 && it.columnSize != 255 || !it.nullable) {
                    if (it.columnSize > 0 && it.columnSize != 255) {
                        columnAnnotation += ", length = ${it.columnSize}"
                    }
                    if (!it.nullable) {
                        columnAnnotation += ", nullable = false"
                    }
                }
                columnAnnotation += ")"
                annotation(columnAnnotation)
                if (it.numericBooleanType) {
                    annotation("@org.hibernate.annotations.Type(type = \"org.hibernate.type.NumericBooleanType\")")
                }
            }


            //getter
            method("get${it.javaName.capitalized()}", it.javaType) {
                if (it.remark.isNotBlank() || it.columnDef != null)
                    javadoc {
                        +"/**"
                        +" * ${it.returnRemark}"
                        +" */"
                    }
                +"return ${it.javaName};"
            }
            //setter
            method("set${it.javaName.capitalized()}", primaryKeyType) {
                if (it.remark.isNotBlank() || it.columnDef != null)
                    javadoc {
                        +"/**"
                        +" * ${it.paramRemark}"
                        +" * @return ${remarks}实例"
                        +" */"
                    }
                parameter {
                    type = it.javaType
                    name = it.javaName
                }
                +"this.${it.javaName} = ${it.javaName};"
                +"return this;"
            }
        }
        //equals
        method(
            "equals",
            JavaType.booleanPrimitiveInstance,
            Parameter("o", JavaType.objectInstance)
        ) {
            annotation("@Override")
            +"if (this == o) {"
            +"return true;"
            +"}"
            +"if (!(o instanceof ${primaryKeyClassName})) {"
            +"return false;"
            +"}"
            +"${primaryKeyClassName} that = (${primaryKeyClassName}) o;"
            val size = primaryKeys.size
            primaryKeys.forEachIndexed { index, column ->
                when (index) {
                    0 -> {
                        +"return Objects.equals(${column.javaName}, that.${column.javaName}) &&"
                    }
                    size - 1 -> {
                        +"    Objects.equals(${column.javaName}, that.${column.javaName});"
                    }
                    else -> {
                        +"    Objects.equals(${column.javaName}, that.${column.javaName}) &&"
                    }
                }
            }
        }

        //hashCode
        method("hashCode", JavaType.intPrimitiveInstance) {
            annotation("@Override")
            +"return Objects.hash(${primaryKeys.joinToString(", ") { it.javaName }});"
        }

        //toString
        import("top.bettercode.lang.util.StringUtil")
        method("toString", JavaType.stringInstance) {
            annotation("@Override")
            +"return StringUtil.json(this);"
        }
    }
}