import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dom.java.element.Parameter

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
            superClass = JavaType("cn.bestwu.simpleframework.data.jpa.BaseServiceImpl").typeArgument(entityType, primaryKeyType, repositoryType)

            implement(iserviceType)

            //constructor
            constructor(Parameter("repository", repositoryType)) {
                +"super(repository);"
            }
        }

    }
}