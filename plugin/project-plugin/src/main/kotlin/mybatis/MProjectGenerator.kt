import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dsl.Generator

/**
 *
 * @author Peter Wu
 */
abstract class MProjectGenerator : Generator() {

    /**
     * 是否有主键
     */
    val hasPrimaryKey: Boolean
        get() = table.primaryKeys.isNotEmpty()


    override val primaryKeyType: JavaType
        get() {
            return primaryKey.javaType
        }

    val daoXml
        get() =
            (table.module) + if (extension.userModule && table.subModule.isNotBlank()) {
                "${table.subModule}/$projectEntityName.xml"
            } else {
                "${projectEntityName}.xml"
            }

    val modulePackageInfoType get() = JavaType("$packageName.package-info")
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
    val daoType get() = JavaType("$packageName.${modulePackage("Dao")}.I${projectClassName}Dao")

}


