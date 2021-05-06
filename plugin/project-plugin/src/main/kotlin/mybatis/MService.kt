import cn.bestwu.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class MService : MModuleJavaGenerator() {

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
            superClass = JavaType("cn.bestwu.simpleframework.data.BaseServiceImpl").typeArgument(daoType, entityType)
        }
    }
}