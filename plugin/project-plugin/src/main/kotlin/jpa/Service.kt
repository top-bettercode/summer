import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Parameter

/**
 * @author Peter Wu
 */
open class Service : ModuleJavaGenerator() {
    override val type: JavaType
        get() = serviceType

    override fun content() {
        clazz {
            annotation("@org.springframework.stereotype.Service")
            javadoc {
                +"/**"
                +" * $remarks 服务层实现"
                +" */"
            }
            superClass = if (hasPrimaryKey) {
                JavaType("top.bettercode.simpleframework.data.jpa.BaseService").typeArgument(
                    entityType,
                    primaryKeyType,
                    repositoryType
                )
            }else{
                JavaType("top.bettercode.simpleframework.data.jpa.BaseSimpleService").typeArgument(
                    repositoryType
                )
            }

            //constructor
            constructor(Parameter("repository", repositoryType)) {
                +"super(repository);"
            }
        }
    }
}