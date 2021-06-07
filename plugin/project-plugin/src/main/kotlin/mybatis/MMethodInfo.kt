import top.bettercode.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
class MMethodInfo : ModuleJavaGenerator() {

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
            columns.forEach {
                //getter
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
    }
}