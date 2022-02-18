import top.bettercode.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class MIService : MProjectGenerator() {

    override fun content() {
        interfaze(iserviceType) {
            javadoc {
                +"/**"
                +" * $remarks 服务层"
                +" */"
            }
            if (hasPrimaryKey) {
                val superInterface =
                    JavaType("top.bettercode.simpleframework.data.IBaseService").typeArgument(
                        daoType,
                        entityType
                    )
                implement(superInterface)
            }
        }
    }
}