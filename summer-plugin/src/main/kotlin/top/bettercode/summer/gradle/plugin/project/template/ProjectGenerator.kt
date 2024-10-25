package top.bettercode.summer.gradle.plugin.project.template

import top.bettercode.summer.tools.generator.DatabaseDriver
import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.lang.util.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.generator.dsl.Generator
import top.bettercode.summer.tools.lang.CharSequenceExtensions.capitalized
import java.util.*

/**
 *
 * @author Peter Wu
 */
abstract class ProjectGenerator : Generator() {

    fun columnAnnotation(column: Column, driver: DatabaseDriver): String {
        val columnDefinition = ""
        var columnName = column.columnName
        if (driver in arrayOf(DatabaseDriver.MYSQL, DatabaseDriver.MARIADB) && MYSQL_KEYWORDS.contains(columnName.uppercase(Locale.getDefault()))) {
            columnName = "`$columnName`"
        }

        var columnAnnotation =
                "@javax.persistence.Column(name = \"$columnName\"$columnDefinition"
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
    val dataObjectType get() = JavaType("$packageName.${modulePackage("DataObject")}.${projectClassName}DO")
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


    companion object {
        val MYSQL_KEYWORDS: Array<String> = arrayOf(
                "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN",
                "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR",
                "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION", "CONSTRAINT", "CONTINUE", "CONVERT",
                "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER",
                "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND",
                "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC",
                "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH", "ELSE", "ELSEIF",
                "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FLOAT4",
                "FLOAT8", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT", "GENERATED", "GET", "GRANT", "GROUP",
                "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE",
                "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INT1", "INT2",
                "INT3", "INT4", "INT8", "INTEGER", "INTERVAL", "INTO", "IS", "ITERATE", "JOIN", "KEY", "KEYS",
                "KILL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINEAR", "LINES", "LOAD", "LOCALTIME",
                "LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MASTER_BIND",
                "MASTER_SSL_VERIFY_SERVER_CERT", "MATCH", "MAXVALUE", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT",
                "MIDDLEINT", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT",
                "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE", "OPTIMIZER_COSTS", "OPTION",
                "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "PARTITION", "PLAN", "PRECISION",
                "PRIMARY", "PROCEDURE", "PURGE", "RANGE", "READ", "READS", "READ_WRITE", "REAL", "REFERENCES",
                "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESIGNAL", "RESTRICT",
                "RETURN", "REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT",
                "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SIGNAL", "SLOW", "SMALLINT", "SPATIAL", "SPECIFIC",
                "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS",
                "SQL_SMALL_RESULT", "SSL", "STARTING", "STRAIGHT_JOIN", "TABLE", "TERMINATED", "THEN",
                "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO", "UNION",
                "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME",
                "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "WHEN",
                "WHERE", "WHILE", "WITH", "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL"
        )

    }
}


