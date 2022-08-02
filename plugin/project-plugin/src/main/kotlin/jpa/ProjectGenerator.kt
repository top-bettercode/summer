import top.bettercode.generator.DatabaseDriver
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dsl.Generator

/**
 *
 * @author Peter Wu
 */
abstract class ProjectGenerator : Generator() {


    fun columnAnnotation(column: Column): String {
        val columnDefinition = if (datasource.databaseDriver == DatabaseDriver.H2) "" else
            ", columnDefinition = \"${column.typeDesc}${column.defaultDesc}${if (column.extra.isBlank()) "" else " ${column.extra}"}\""
        var columnAnnotation =
            "@javax.persistence.Column(name = \"${column.columnName}\"$columnDefinition"
        if (column.columnSize > 0 && column.columnSize != 255) {
            if (column.javaType == JavaType.stringInstance)
                columnAnnotation += ", length = ${column.columnSize}"
            if (column.javaType == JavaType("java.math.BigDecimal"))
                columnAnnotation += ", precision = ${column.columnSize}"
        }
        if (column.javaType == JavaType("java.math.BigDecimal") && column.decimalDigits > 0) {
            columnAnnotation += ", scale = ${column.decimalDigits}"
        }
        if (!column.nullable) {
            columnAnnotation += ", nullable = false"
        }
        columnAnnotation += ")"
        return columnAnnotation
    }

    val interfaceService get() = enable("interfaceService", false)

    val isCore get() = projectName == "core"

    val msgName: String
        get() {
            return "${if (isCore) "core-" else ""}messages.properties"
        }
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
            "$packageName.${modulePackage("Properties")}.${
                table.className
            }Properties"
        )
    val matcherType
        get() = JavaType(
            "$packageName.${modulePackage("Matcher")}.${
                table.className
            }Matcher"
        )
    val methodInfoType
        get() = JavaType(
            "$packageName.${modulePackage("MethodInfo")}.${
                table.className
            }MethodInfo"
        )
    val formType get() = JavaType("$packageName.${modulePackage("Form")}.${projectClassName}Form")
    val mixInType get() = JavaType("$packageName.${modulePackage("MixIn")}.${projectClassName}MixIn")
    val controllerType get() = JavaType("$packageName.${modulePackage("Controller")}.${projectClassName}Controller")
    val updateControllerType get() = JavaType("$packageName.${modulePackage("Controller")}.${projectClassName}UpdateController")
    val controllerTestType get() = JavaType("$packageName.${modulePackage("ControllerTest")}.${projectClassName}ControllerTest")
    val iserviceType get() = JavaType("$packageName.${modulePackage("Service")}.I${projectClassName}Service")
    val serviceType get() = JavaType("$packageName.${modulePackage("Service")}.${projectClassName}Service")
    val testServiceType get() = JavaType("$packageName.${modulePackage("Service")}.${projectClassName}TestService")
    val repositoryType get() = JavaType("$packageName.${modulePackage("Repository")}.${projectClassName}Repository")


}


