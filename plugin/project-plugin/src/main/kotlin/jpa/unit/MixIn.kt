package jpa.unit

import ProjectGenerator
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Interface
import java.util.*

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
            method(
                "get${primaryKeyName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                primaryKeyType
            ) {
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
        }

        columns.filter { it.javaName != primaryKeyName }.forEach {
            //getter
            getter(this, it)
        }
    }
}
private val getter: ProjectGenerator.(Interface, Column) -> Unit = { interfaze, it ->
    interfaze.apply {
        if (it.jsonViewIgnored)
            method(
                "get${it.javaName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                it.javaType
            ) {
                annotation("@com.fasterxml.jackson.annotation.JsonIgnore")
                annotation("@Override")
            }

        //code
        if (!it.softDelete && it.isCodeField) {
            method(
                "get${it.javaName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                it.javaType
            ) {
                if (it.columnName.contains("_") || it.softDelete)
                    annotation("@top.bettercode.simpleframework.web.serializer.annotation.JsonCode")
                else {
                    import(
                        "${
                            (ext.packageName + ".support.dic." + className + it.javaName.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            })
                        }Enum"
                    )
                    annotation(
                        "@top.bettercode.simpleframework.web.serializer.annotation.JsonCode(${
                            (className + it.javaName.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            })
                        }Enum.ENUM_NAME)"
                    )
                }
                annotation("@Override")
            }
        }
    }
}