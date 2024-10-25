package top.bettercode.summer.gradle.plugin.project.template.unit

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.Parameter
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.lang.CharSequenceExtensions.capitalized

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

        val dataObjectClass = dataObjectType.shortName

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
                    dataObjectType,
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
            +"if (!(o instanceof ${dataObjectClass})) {"
            +"return false;"
            +"}"
            +"$dataObjectClass that = (${dataObjectClass}) o;"
            val size = columns.size
            if (size == 1) {
                +"return Objects.equals(${columns[0].javaName}, that.${columns[0].javaName});"
            } else {
                columns.forEachIndexed { index, column ->
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

        //hashCode
        method("hashCode", JavaType.int) {
            annotation("@Override")
            +"return Objects.hash(${columns.joinToString(", ") { it.javaName }});"
        }

        //toString
        import("top.bettercode.summer.tools.lang.util.StringUtil")
        method("toString", JavaType.stringInstance) {
            annotation("@Override")
            +"return StringUtil.json(this);"
        }
    }
}