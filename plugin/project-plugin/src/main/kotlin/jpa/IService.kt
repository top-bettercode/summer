import cn.bestwu.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class IService : ModuleJavaGenerator() {
    override val type: JavaType
        get() = iServiceType

    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks 服务层"
                +" */"
            }
            val superInterface = JavaType("cn.bestwu.simpleframework.data.jpa.IBaseService").typeArgument(entityType, primaryKeyType, repositoryType)
            implement(superInterface)
        }
    }
}