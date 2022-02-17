import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dsl.JavaGenerator

/**
 *
 * @author Peter Wu
 */
abstract class ModuleJavaGenerator : JavaGenerator() {

    private fun modulePackage(name: String): String {
        val onePackage = enable("onePackage", true)
        return if (onePackage)
            table.entityName(extension).toLowerCase()
        else when (name) {
            "Entity", "Properties", "Matcher" -> "entity"
            "MethodInfo" -> "info"
            "Form" -> "form"
            "MixIn" -> "response.mixin"
            "Controller", "ControllerTest" -> "controller"
            "IService" -> "service"
            "Service" -> "service"
            "ServiceImpl" -> "service.impl"
            "Repository" -> "repository"
            else -> table.entityName(extension).toLowerCase()
        }
    }

    protected val modulePackageInfoType get() = JavaType("$packageName.package-info")
    protected val packageInfoType get() = JavaType("$packageName.${modulePackage("Entity")}.package-info")
    override val primaryKeyType: JavaType
        get() {
            return if (primaryKeys.size == 1) {
                primaryKey.javaType
            } else {
                JavaType(
                    "$packageName.${modulePackage("Entity")}.${
                        if (isFullComposite) table.className(extension)
                        else "${
                            table.className(extension)
                        }Key"
                    }"
                )
            }
        }
    protected val entityType get() = JavaType("$packageName.${modulePackage("Entity")}.${className}")
    protected val propertiesType
        get() = JavaType(
            "$packageName.${modulePackage("Properties")}.${className}Properties"
        )
    protected val matcherType
        get() = JavaType(
            "$packageName.${modulePackage("Matcher")}.${className}Matcher"
        )
    protected val methodInfoType
        get() = JavaType(
            "$packageName.${modulePackage("MethodInfo")}.${
                table.className(
                    extension
                )
            }MethodInfo"
        )
    protected val formType get() = JavaType("$packageName.${modulePackage("Form")}.${projectClassName}Form")
    protected val mixInType get() = JavaType("$packageName.${modulePackage("MixIn")}.${projectClassName}MixIn")
    protected val controllerType get() = JavaType("$packageName.${modulePackage("Controller")}.${projectClassName}Controller")
    protected val controllerTestType get() = JavaType("$packageName.${modulePackage("ControllerTest")}.${projectClassName}ControllerTest")
    protected val iserviceType get() = JavaType("$packageName.${modulePackage("Service")}.I${projectClassName}Service")
    protected val serviceType get() = JavaType("$packageName.${modulePackage("Service")}.${projectClassName}Service")
    protected val serviceImplType get() = JavaType("$packageName.${modulePackage("ServiceImpl")}.${projectClassName}ServiceImpl")
    protected val repositoryType get() = JavaType("$packageName.${modulePackage("Repository")}.${projectClassName}Repository")


}


