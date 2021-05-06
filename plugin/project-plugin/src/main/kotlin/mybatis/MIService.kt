import cn.bestwu.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class MIService : MModuleJavaGenerator() {

    override val type: JavaType
        get() = iServiceType

    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks 服务层"
                +" */"
            }
            val superInterface =
                JavaType("cn.bestwu.simpleframework.data.IBaseService").typeArgument(
                    daoType,
                    entityType
                )
            implement(superInterface)
        }
    }
}