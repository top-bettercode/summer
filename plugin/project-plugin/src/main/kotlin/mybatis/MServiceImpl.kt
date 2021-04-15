import cn.bestwu.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class MServiceImpl : MModuleJavaGenerator() {

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
            superClass = JavaType("cn.bestwu.simpleframework.data.BaseServiceImpl").typeArgument(JavaType("$packageName.dao.I${projectClassName}Dao"), entityType)


            val impl = "$packageName.service.I${projectClassName}Service"
            implement(impl)
        }

    }
}