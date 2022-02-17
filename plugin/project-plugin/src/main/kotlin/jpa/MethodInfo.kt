import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Interface

/**
 * @author Peter Wu
 */
class MethodInfo : ModuleJavaGenerator() {

    override var cover: Boolean = true
    override val type: JavaType
        get() = methodInfoType


    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks"
                +" */"
            }
            if (!isFullComposite) {
                //primaryKey getter
                method("get${primaryKeyName.capitalize()}", primaryKeyType) {
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

    val getter: Interface.(Column) -> Unit = {
        method("get${it.javaName.capitalize()}", it.javaType) {
            if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                javadoc {
                    +"/**"
                    +" * ${getReturnRemark(it)}"
                    +" */"
                }
        }
    }
}