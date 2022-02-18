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
            "jpa.unit.Repository" -> "repository"
            else -> table.entityName(extension).toLowerCase()
        }
    }

    val primaryKeyType: JavaType
        get() {
            return if (primaryKeys.size == 1) {
                primaryKey.javaType
            } else {
                JavaType(
                    "$packageName.${modulePackage("Entity")}.${
                        if (isFullComposite) table.className(extension)
                        else "${table.className(extension)}Key"
                    }"
                )
            }
        }
    val msgName get() = "${if (projectName == "core") "core-" else ""}messages.properties"
    val mapperXmlName
        get() = "${
            repositoryType.fullyQualifiedNameWithoutTypeParameters.replace(
                ".",
                "/"
            )
        }.xml"

    val modulePackageInfoType get() = JavaType("$packageName.package-info")
    val packageInfoType get() = JavaType("$packageName.${modulePackage("Entity")}.package-info")

    val entityType get() = JavaType("$packageName.${modulePackage("Entity")}.${className}")
    val propertiesType
        get() = JavaType(
            "$packageName.${modulePackage("Properties")}.${className}Properties"
        )
    val matcherType
        get() = JavaType(
            "$packageName.${modulePackage("Matcher")}.${className}Matcher"
        )
    val methodInfoType
        get() = JavaType(
            "$packageName.${modulePackage("MethodInfo")}.${
                table.className(
                    extension
                )
            }MethodInfo"
        )
    val formType get() = JavaType("$packageName.${modulePackage("Form")}.${projectClassName}Form")
    val mixInType get() = JavaType("$packageName.${modulePackage("MixIn")}.${projectClassName}MixIn")
    val controllerType get() = JavaType("$packageName.${modulePackage("Controller")}.${projectClassName}Controller")
    val controllerTestType get() = JavaType("$packageName.${modulePackage("ControllerTest")}.${projectClassName}ControllerTest")
    val iserviceType get() = JavaType("$packageName.${modulePackage("Service")}.I${projectClassName}Service")
    val serviceType get() = JavaType("$packageName.${modulePackage("Service")}.${projectClassName}Service")
    val serviceImplType get() = JavaType("$packageName.${modulePackage("ServiceImpl")}.${projectClassName}ServiceImpl")
    val repositoryType get() = JavaType("$packageName.${modulePackage("jpa.unit.Repository")}.${projectClassName}jpa.unit.Repository")


}


