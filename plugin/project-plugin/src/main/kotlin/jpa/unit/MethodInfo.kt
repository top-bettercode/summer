package jpa.unit

import ProjectGenerator
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.element.Interface
import java.util.*

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
        if (!isFullComposite) {
            //primaryKey getter
            method("get${primaryKeyName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", primaryKeyType) {
                javadoc {
                    +"/**"
                    +" * ${remarks}主键"
                    +" */"
                }
            }
            otherColumns.forEach {
                //getter
                getter(this, it)
            }
        } else {
            columns.forEach {
                //getter
                getter(this, it)
            }
        }
    }


}

private val getter: ProjectGenerator.(Interface, Column) -> Unit = { interfaze, it ->
    interfaze.apply {
        method("get${it.javaName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}", it.javaType) {
            if (it.remark.isNotBlank() || it.columnDef != null)
                javadoc {
                    +"/**"
                    +" * ${it.returnRemark}"
                    +" */"
                }
        }
    }
}
