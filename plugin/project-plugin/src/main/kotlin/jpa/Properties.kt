import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility

/**
 * @author Peter Wu
 */
class Properties : ModuleJavaGenerator() {

    override var cover: Boolean = true
    override val type: JavaType
        get() = propertiesType


    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks"
                +" */"
            }
            columns.forEach {
                //getter
                field(it.javaName, JavaType.stringInstance, "\"${it.javaName}\"") {
                    visibility = JavaVisibility.DEFAULT
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * ${getRemark(it)}"
                            +" */"
                        }
                }
            }
        }
    }
}