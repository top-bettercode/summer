/**
 * @author Peter Wu
 */
class MMethodInfo : ModuleJavaGenerator() {

    override fun content() {
        interfaze(methodInfoType, true) {
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