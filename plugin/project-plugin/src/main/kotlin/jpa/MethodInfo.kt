import top.bettercode.generator.dom.java.JavaType

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