package top.bettercode.summer.gradle.plugin.project.template.unit

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.dom.java.element.Interface
import top.bettercode.summer.tools.generator.dsl.Generator.Companion.enumClassName
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.util.JavaType


/**
 * @author Peter Wu
 */
val mixIn: ProjectGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/** $remarks */"
        }
        implement(
            JavaType("top.bettercode.summer.web.serializer.MixIn").typeArgument(
                if (isFullComposite) primaryKeyType else entityType
            ), methodInfoType, serializationViewsType
        )

        if (!isFullComposite) {
            //primaryKey getter
            method(
                "get${primaryKeyName.capitalized()}",
                primaryKeyType
            ) {
                javadoc {
                    +"/** ${remarks}主键 */"
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
        if (!it.logicalDelete && it.isCodeField) {
            val codeType = it.codeType
            method(
                "get${it.javaName.capitalized()}",
                it.javaType
            ) {
                if (it.javaName == codeType)
                    annotation("@top.bettercode.summer.web.serializer.annotation.JsonCode")
                else {
                    import("${(ext.packageName + ".support.dic." + enumClassName(codeType))}Enum")

                    annotation(
                        "@top.bettercode.summer.web.serializer.annotation.JsonCode(${
                            enumClassName(
                                codeType
                            )
                        }Enum.ENUM_NAME)"
                    )
                }
                annotation("@Override")
            }
        }
    }
}

val serializationViews: ProjectGenerator.(Interface) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/** 模型属性 json SerializationViews */"
        }
        implement(coreSerializationViewsType)
    }
}
