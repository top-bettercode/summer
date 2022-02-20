package jpa.unit

import ProjectGenerator
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.Parameter
import top.bettercode.generator.dom.java.element.TopLevelClass

/**
 *
 * @author Peter Wu
 */
val compositePrimaryKey: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        import("java.util.Objects")
        import("javax.persistence.Embeddable")
        import("top.bettercode.lang.util.StringUtil")
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

        val keySep = "_"
        if (!isFullComposite) {
            import("org.springframework.util.Assert")
            constructor(Parameter(primaryKeyName, JavaType.stringInstance)) {
                +"Assert.hasText(${primaryKeyName}, \"${primaryKeyName}不能为空\");"
                +"String[] split = ${primaryKeyName}.split(\"${keySep}\");"
                +"Assert.isTrue(split.length == ${primaryKeys.size}, \"${primaryKeyName}格式不对\");"
                primaryKeys.forEachIndexed { index, column ->
                    +"this.${column.javaName} = ${column.setValue("split[${index}]")};"
                }
            }
        }

        primaryKeys.forEach {
            //field
            field(it.javaName, it.javaType) {
                if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                    javadoc {
                        +"/**"
                        +" * ${getRemark(it)}"
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
            }


            //getter
            method("get${it.javaName.capitalize()}", it.javaType) {
                if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                    javadoc {
                        +"/**"
                        +" * ${getReturnRemark(it)}"
                        +" */"
                    }
                +"return ${it.javaName};"
            }
            //setter
            method("set${it.javaName.capitalize()}", primaryKeyType) {
                if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                    javadoc {
                        +"/**"
                        +" * ${getParamRemark(it)}"
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
        if (isFullComposite)
            method("toString", JavaType.stringInstance) {
                annotation("@Override")
                +"return \"${className}{\" +"
                primaryKeys.forEachIndexed { _, it ->
                    +"    \"${it.javaName}=${if (it.javaType == JavaType.stringInstance) "'" else ""}\" + ${it.javaName} ${if (it.javaType == JavaType.stringInstance) "+ '\\'' " else ""}+"
                }
                +"    '}';"
            }
        else
            method("toString", JavaType.stringInstance) {
                annotation("@Override")
                +"return ${primaryKeys.joinToString(" + \"${keySep}\" + ") { it.javaName }};"
            }
    }
}