package jpa.unit

import ProjectGenerator
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Interface

/**
 * @author Peter Wu
 */
val mixIn: ProjectGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * $remarks"
            +" */"
        }
        val serializationViews = JavaType("$basePackageName.web.SerializationViews")
        implement(
            JavaType("top.bettercode.simpleframework.web.serializer.MixIn").typeArgument(
                if (isFullComposite) primaryKeyType else entityType
            ), methodInfoType, serializationViews
        )

        if (!isFullComposite) {
            //primaryKey getter
            method("get${primaryKeyName.capitalize()}", primaryKeyType) {
                javadoc {
                    +"/**"
                    +" * ${remarks}主键"
                    +" */"
                }
                annotation("@com.fasterxml.jackson.annotation.JsonView(Object.class)")
                if (isCompositePrimaryKey)
                    annotation("@top.bettercode.simpleframework.web.serializer.annotation.JsonEmbeddedId")
                annotation("@Override")
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
        if (it.jsonViewIgnored)
            method("get${it.javaName.capitalize()}", it.javaType) {
                if (it.jsonViewIgnored)
                    annotation("@com.fasterxml.jackson.annotation.JsonIgnore")

                annotation("@Override")
            }

        //code
        if (!it.jsonViewIgnored && it.isCodeField) {
            method("get${it.javaName.capitalize()}", it.javaType) {
                if (it.columnName.contains("_") || ext.softDeleteColumnName == it.columnName)
                    annotation("@top.bettercode.simpleframework.web.serializer.annotation.JsonCode")
                else {
                    import("${(ext.packageName + ".support.dic." + className + it.javaName.capitalize())}Enum")
                    annotation("@top.bettercode.simpleframework.web.serializer.annotation.JsonCode(${(className + it.javaName.capitalize())}Enum.ENUM_NAME)")
                }
                annotation("@Override")
            }
        }
    }
}