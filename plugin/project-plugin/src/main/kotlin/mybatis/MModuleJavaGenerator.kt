import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dsl.JavaGenerator

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

    /**
     * 是否有主键
     */
    val hasPrimaryKey: Boolean
        get() = table.primaryKeys.isNotEmpty()


    val primaryKeyType: JavaType
        get() {
            return if (primaryKeys.size == 1) {
                primaryKey.javaType
            } else {
                JavaType("$packageName.domain.${className}.${className}Key")
            }
        }

    val daoXml
        get() = (if (extension.defaultModule) "mapper" else extension.module) + if (extension.userModule && subModule.isNotBlank()) {
            "$subModule/$projectEntityName.xml"
        } else {
            "${projectEntityName}.xml"
        }

    val modulePackageInfoType get() = JavaType("$packageName.package-info")
    val packageInfoType get() = JavaType("$packageName.${modulePackage("Entity")}.package-info")
    val entityType get() = JavaType("$packageName.${modulePackage("Entity")}.$className")
    val queryDslType get() = JavaType("$packageName.${modulePackage("QueryDsl")}.Q$className")
    val methodInfoType get() = JavaType("$packageName.${modulePackage("MethodInfo")}.${className}MethodInfo")
    val formType get() = JavaType("$packageName.${modulePackage("Form")}.${projectClassName}Form")
    val mixInType get() = JavaType("$packageName.${modulePackage("MixIn")}.${projectClassName}MixIn")
    val controllerType get() = JavaType("$packageName.${modulePackage("Controller")}.${projectClassName}Controller")
    val controllerTestType get() = JavaType("$packageName.${modulePackage("ControllerTest")}.${projectClassName}ControllerTest")
    val iserviceType get() = JavaType("$packageName.${modulePackage("Service")}.I${projectClassName}Service")
    val serviceType get() = JavaType("$packageName.${modulePackage("Service")}.${projectClassName}Service")
    val serviceImplType get() = JavaType("$packageName.${modulePackage("ServiceImpl")}.${projectClassName}ServiceImpl")
    val repositoryType get() = JavaType("$packageName.${modulePackage("jpa.unit.Repository")}.${projectClassName}jpa.unit.Repository")
    val daoType get() = JavaType("$packageName.${modulePackage("Dao")}.I${projectClassName}Dao")

}


