package jpa.unit

import ProjectGenerator
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Interface
import top.bettercode.generator.dom.java.element.JavaVisibility

/**
 *
 * @author Peter Wu
 */
val properties: ProjectGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
        visibility = JavaVisibility.PUBLIC
        if (isCompositePrimaryKey) {
            primaryKeys.forEach {
                field(
                    it.javaName,
                    JavaType.stringInstance,
                    "\"${primaryKeyName}.${it.javaName}\""
                ) {
                    visibility = JavaVisibility.DEFAULT
                    if (it.remark.isNotBlank() || it.columnDef != null)
                        javadoc {
                            +"/**"
                            +" * ${it.docRemark}"
                            +" */"
                        }
                }
            }
        } else {
            field(primaryKeyName, JavaType.stringInstance, "\"${primaryKeyName}\"") {
                visibility = JavaVisibility.DEFAULT
                if (primaryKey.remark.isNotBlank() || primaryKey.columnDef != null)
                    javadoc {
                        +"/**"
                        +" * ${primaryKey.docRemark}"
                        +" */"
                    }
            }
        }
        otherColumns.forEach {
            field(it.javaName, JavaType.stringInstance, "\"${it.javaName}\"") {
                visibility = JavaVisibility.DEFAULT
                if (it.remark.isNotBlank() || it.columnDef != null)
                    javadoc {
                        +"/**"
                        +" * ${it.docRemark}"
                        +" */"
                    }
            }
        }
    }
}