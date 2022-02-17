import top.bettercode.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class IService : ModuleJavaGenerator() {
    override val type: JavaType
        get() = iserviceType

    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks 服务层"
                +" */"
            }
            val superInterface =
                JavaType("top.bettercode.simpleframework.data.jpa.IBaseService").typeArgument(
                    entityType,
                    primaryKeyType,
                    repositoryType
                )
            implement(superInterface)
        }
    }
}