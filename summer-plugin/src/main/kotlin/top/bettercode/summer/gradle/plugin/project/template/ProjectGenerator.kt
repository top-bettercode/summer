package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.generator.dsl.Generator
import top.bettercode.summer.tools.lang.capitalized

/**
 *
 * @author Peter Wu
 */
abstract class ProjectGenerator : Generator() {


    fun columnAnnotation(column: Column): String {
        val columnDefinition = ""
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

    val TopLevelClass.defaultSort: String
        get() {
            var defaultSort = ""
            if (columns.any { it.javaName == "createdDate" } || !isCompositePrimaryKey) {
                import(propertiesType)
                import("org.springframework.data.domain.Sort.Direction")
                defaultSort = "(sort = {"
                if (columns.any { it.javaName == "createdDate" }) {
                    defaultSort += "${propertiesType.shortName}.createdDate"
                    if (!isCompositePrimaryKey) {
                        defaultSort += ", ${propertiesType.shortName}.${primaryKeyName}"
                    }
                    defaultSort += "}"
                } else if (!isCompositePrimaryKey) {
                    defaultSort += "${propertiesType.shortName}.${primaryKeyName}}"
                }
                defaultSort += ", direction = Direction.DESC)"
            }
            return defaultSort
        }

    val testInsertName: String
        get() =
            "${projectEntityName}TestService.insert${
                pathName.capitalized()
            }"


    val interfaceService get() = enable("interfaceService", false)

    val isCore get() = ext.isCore

    val msgName: String
        get() {
            return "core-messages.properties"
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
    val coreSerializationViewsType get() = JavaType("${basePackageName}.web.CoreSerializationViews")
    val serializationViewsType get() = JavaType("$basePackageName.web.${shortProjectName.capitalized()}SerializationViews")
    val mixInType get() = JavaType("$packageName.${modulePackage("MixIn")}.${projectClassName}MixIn")
    val appControllerType get() = JavaType("$basePackageName.support.${shortProjectName}Controller")
    val controllerType get() = JavaType("$packageName.${modulePackage("Controller")}.${projectClassName}Controller")
    val controllerTestType get() = JavaType("$packageName.${modulePackage("ControllerTest")}.${projectClassName}ControllerTest")
    val baseWebTestType get() = JavaType("$basePackageName.support.BaseWebTest")
    val iserviceType get() = JavaType("$packageName.${modulePackage("Service")}.I${projectClassName}Service")
    val serviceType get() = JavaType("$packageName.${modulePackage("Service")}.${projectClassName}Service")
    val testServiceType get() = JavaType("$packageName.${modulePackage("Service")}.${projectClassName}TestService")
    val repositoryType get() = JavaType("$packageName.${modulePackage("Repository")}.${projectClassName}Repository")


}


