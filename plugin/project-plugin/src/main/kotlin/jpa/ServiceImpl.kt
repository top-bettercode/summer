import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Parameter

/**
 * @author Peter Wu
 */
open class ServiceImpl : ModuleJavaGenerator() {

    override val type: JavaType
        get() = serviceImplType

    override fun content() {
        clazz {
            annotation("@org.springframework.stereotype.Service")
            javadoc {
                +"/**"
                +" * $remarks 服务层实现"
                +" */"
            }
            superClass = JavaType("top.bettercode.simpleframework.data.jpa.BaseService").typeArgument(entityType, primaryKeyType, repositoryType)

            implement(iserviceType)

            //constructor
            constructor(Parameter("repository", repositoryType)) {
                +"super(repository);"
            }
        }

    }
}