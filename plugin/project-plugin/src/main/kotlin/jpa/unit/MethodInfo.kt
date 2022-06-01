package jpa.unit

import ProjectGenerator
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.element.Interface
import top.bettercode.lang.capitalized

/**
 * @author Peter Wu
 */
val methodInfo: ProjectGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * $remarks"
            +" */"
        }
        if (isCompositePrimaryKey) {
            //primaryKey getter
            method("get${primaryKeyName.capitalized()}", primaryKeyType) {
                javadoc {
                    +"/**"
                    +" * ${remarks}主键"
                    +" */"
                }
            }
        }

        columns.forEach {
            //getter
            getter(this, it)
        }
    }


}

private val getter: ProjectGenerator.(Interface, Column) -> Unit = { interfaze, it ->
    interfaze.apply {
        method("get${it.javaName.capitalized()}", it.javaType) {
            if (it.remark.isNotBlank() || it.columnDef != null)
                javadoc {
                    +"/**"
                    +" * ${it.returnRemark}"
                    +" */"
                }
        }
    }
}
