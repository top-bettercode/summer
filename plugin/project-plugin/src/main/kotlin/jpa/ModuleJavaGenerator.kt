import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dsl.Generator

/**
 *
 * @author Peter Wu
 */
abstract class ModuleJavaGenerator : Generator() {

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
    val serviceType get() = JavaType("$packageName.${modulePackage("Service")}.${projectClassName}Service")
    val repositoryType get() = JavaType("$packageName.${modulePackage("Repository")}.${projectClassName}Repository")


}


