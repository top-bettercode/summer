import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Interface
import top.bettercode.generator.dom.java.element.TopLevelClass

/**
 * @author Peter Wu
 */
open class MixIn : ModuleJavaGenerator() {

    override val type: JavaType
        get() = mixInType


    override fun content() {
        interfaze {
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
                    if (compositePrimaryKey)
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

    val getter: Interface.(Column) -> Unit = {
        if (it.jsonViewIgnored)
            method("get${it.javaName.capitalize()}", it.javaType) {
                if (it.jsonViewIgnored)
                    annotation("@com.fasterxml.jackson.annotation.JsonIgnore")

                annotation("@Override")
            }

        //code
        if (!it.jsonViewIgnored && it.isCodeField) {
            method("get${it.javaName.capitalize()}", it.javaType) {
                if (it.columnName.contains("_") || extension.softDeleteColumnName == it.columnName)
                    annotation("@top.bettercode.simpleframework.web.serializer.annotation.JsonCode")
                else {
                    import("${(extension.packageName + ".support.dic." + className + it.javaName.capitalize())}Enum")
                    annotation("@top.bettercode.simpleframework.web.serializer.annotation.JsonCode(${(className + it.javaName.capitalize())}Enum.ENUM_NAME)")
                }
                annotation("@Override")
            }
        }
    }
}