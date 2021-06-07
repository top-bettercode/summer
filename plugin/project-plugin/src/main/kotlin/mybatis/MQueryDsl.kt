
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility

/**
 * @author Peter Wu
 */
class MQueryDsl : MModuleJavaGenerator() {

    override var cover: Boolean = true
    override val type: JavaType
        get() = queryDslType

    override fun content() {
        clazz {
            javadoc {
                +"/**"
                +" * $remarks 对应表名：$tableName"
                +" */"
            }
            superClass = JavaType("top.bettercode.simpleframework.data.dsl.EntityPathWrapper").typeArgument(type, entityType)

            serialVersionUID()

            val basePathType = JavaType("top.bettercode.simpleframework.data.dsl.BasePath").typeArgument(type, entityType)
            columns.forEach {
                //field
                field(it.javaName, basePathType, "new BasePath<>(this, \"${it.columnName}\")", visibility = JavaVisibility.PUBLIC) {
                    isFinal = true
                    if (it.remarks.isNotBlank())
                        if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                            javadoc {
                                +"/**"
                                +" * ${it.remarks}${if (it.columnDef.isNullOrBlank())"" else " 默认值：${it.columnDef}"}"
                                +" */"
                            }
                }
            }

            method("newInstance", type) {
                visibility = JavaVisibility.PUBLIC
                isStatic = true
                javadoc {
                    +"/**"
                    +" * @return 新实例"
                    +" */"
                }
                +"return new Q$className();"
            }
        }
    }
}