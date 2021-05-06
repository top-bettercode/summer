import cn.bestwu.generator.dom.java.JavaType
import cn.bestwu.generator.dsl.JavaGenerator

/**
 *
 * @author Peter Wu
 */
abstract class MModuleJavaGenerator : JavaGenerator() {

    private fun modulePackage(name: String): String {
        val onePackage = enable("onePackage", true)
        return if (onePackage)
            entityName.toLowerCase()
        else when (name) {
            "Entity" -> "domain"
            "QueryDsl" -> "querydsl"
            "MethodInfo" -> "info"
            "Form" -> "form"
            "MixIn" -> "response.mixin"
            "Controller", "ControllerTest" -> "controller"
            "Service" -> "service"
            "ServiceImpl" -> "service.impl"
            "Dao" -> "dao"
            else -> entityName.toLowerCase()
        }
    }

    override val primaryKeyType: JavaType
        get() {
            return if (primaryKeys.size == 1) {
                primaryKey.javaType
            } else {
                JavaType("$packageName.domain.${className}.${className}Key")
            }
        }


    protected val modulePackageInfoType get() = JavaType("$packageName.package-info")
    protected val packageInfoType get() = JavaType("$packageName.${modulePackage("Entity")}.package-info")
    protected val entityType get() = JavaType("$packageName.${modulePackage("Entity")}.$className")
    protected val queryDslType get() = JavaType("$packageName.${modulePackage("QueryDsl")}.Q$className")
    protected val methodInfoType get() = JavaType("$packageName.${modulePackage("MethodInfo")}.${className}MethodInfo")
    protected val formType get() = JavaType("$packageName.${modulePackage("Form")}.${projectClassName}Form")
    protected val mixInType get() = JavaType("$packageName.${modulePackage("MixIn")}.${projectClassName}MixIn")
    protected val controllerType get() = JavaType("$packageName.${modulePackage("Controller")}.${projectClassName}Controller")
    protected val controllerTestType get() = JavaType("$packageName.${modulePackage("ControllerTest")}.${projectClassName}ControllerTest")
    protected val iServiceType get() = JavaType("$packageName.${modulePackage("Service")}.I${projectClassName}Service")
    protected val serviceType get() = JavaType("$packageName.${modulePackage("Service")}.${projectClassName}Service")
    protected val serviceImplType get() = JavaType("$packageName.${modulePackage("ServiceImpl")}.${projectClassName}ServiceImpl")
    protected val repositoryType get() = JavaType("$packageName.${modulePackage("Repository")}.${projectClassName}Repository")
    protected val daoType get() = JavaType("$packageName.${modulePackage("Dao")}.I${projectClassName}Dao")

}


