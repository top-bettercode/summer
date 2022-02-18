import top.bettercode.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class MService : MProjectGenerator() {

    override fun content() {
        clazz(serviceType) {
            annotation("@org.springframework.stereotype.Service")
            javadoc {
                +"/**"
                +" * $remarks 服务层实现"
                +" */"
            }
            superClass = if (hasPrimaryKey) {
                JavaType("top.bettercode.simpleframework.data.BaseService").typeArgument(
                    daoType,
                    entityType
                )
            } else {
                JavaType("top.bettercode.simpleframework.data.BaseSimpleService").typeArgument(
                    daoType
                )
            }

        }
    }
}