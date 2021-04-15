import cn.bestwu.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class MService : MModuleJavaGenerator() {

    override val type: JavaType
        get() = serviceType

    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks 服务层"
                +" */"
            }
            val superInterface = JavaType("cn.bestwu.simpleframework.data.IBaseService").typeArgument(JavaType("$packageName.dao.I${className}Dao"),entityType)
            implement(superInterface)
        }
    }
}