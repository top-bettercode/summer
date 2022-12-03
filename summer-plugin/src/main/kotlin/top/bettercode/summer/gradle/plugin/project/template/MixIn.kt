package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.Interface
import top.bettercode.summer.tools.lang.capitalized

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
        val serializationViews = JavaType("$basePackageName.web.${projectName.capitalized()}SerializationViews")
        implement(
            JavaType("top.bettercode.summer.web.serializer.MixIn").typeArgument(
                if (isFullComposite) primaryKeyType else entityType
            ), methodInfoType, serializationViews
        )

        if (!isFullComposite) {
            //primaryKey getter
            method(
                "get${primaryKeyName.capitalized()}",
                primaryKeyType
            ) {
                javadoc {
                    +"/**"
                    +" * ${remarks}主键"
                    +" */"
                }
                annotation("@com.fasterxml.jackson.annotation.JsonView(Object.class)")
                if (isCompositePrimaryKey)
                    annotation("@top.bettercode.summer.web.serializer.annotation.JsonEmbeddedId")
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
                "get${it.javaName.capitalized()}",
                it.javaType
            ) {
                annotation("@com.fasterxml.jackson.annotation.JsonIgnore")
                annotation("@Override")
            }

        //code
        if (!it.softDelete && it.isCodeField) {
            method(
                "get${it.javaName.capitalized()}",
                it.javaType
            ) {
                if (it.columnName.contains("_") || it.softDelete)
                    annotation("@top.bettercode.summer.web.serializer.annotation.JsonCode")
                else {
                    import(
                        "${
                            (ext.packageName + ".support.dic." + className + it.javaName.capitalized())
                        }Enum"
                    )
                    annotation(
                        "@top.bettercode.summer.web.serializer.annotation.JsonCode(${
                            (className + it.javaName.capitalized())
                        }Enum.ENUM_NAME)"
                    )
                }
                annotation("@Override")
            }
        }
    }
}