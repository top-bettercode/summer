package top.bettercode.summer.tools.generator.dsl

import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.GeneratorException
import top.bettercode.summer.tools.generator.GeneratorExtension
import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.dom.java.element.*
import top.bettercode.summer.tools.generator.dom.unit.*
import top.bettercode.summer.tools.lang.capitalized
import top.bettercode.summer.tools.lang.decapitalized
import top.bettercode.summer.tools.lang.util.BooleanUtil
import top.bettercode.summer.tools.lang.util.JavaType
import java.io.File
import java.util.*

/**
 * 模板基类
 */
open class Generator {

    private val projectUnits: MutableList<GenUnit> = mutableListOf()
    private val units: MutableList<GenUnit> = mutableListOf()

    val Column.defaultRemarks: String
        get() = defaultDesc.replace("DEFAULT ", "默认值：")

    val Column.remark: String
        get() = remarks.replace('?', ' ').trim('.').trim()

    val Column.docRemark: String
        get() = "${
            (if (remark.isBlank()) "" else (if (logicalDelete) remark.split(Regex("[:：,， (（]"))[0] else remark.replace(
                "@",
                "\\@"
            )))
        }${if (columnDef == null || logicalDelete) "" else " 默认值：${if (columnDef!!.isBlank()) "'$columnDef'" else columnDef}"}"

    val Column.paramRemark: String
        get() = if (docRemark.isBlank()) "" else "@param $javaName $docRemark"


    val Column.returnRemark: String
        get() {
            val remark = docRemark.replace(Regex(" ?([:;/]) ?"), " $1 ")
            return if (remark.isBlank()) "" else "@return $remark"
        }


    lateinit var table: Table
    lateinit var ext: GeneratorExtension

    val settings: Map<String, String> get() = ext.settings

    val database: DatabaseConfiguration get() = table.database

    val projectName: String get() = ext.projectName


    val basePackageName: String
        get() = ext.basePackageName

    val packageName: String
        get() {
            var packageName = basePackageName
            if (settings["no-modules"] == null) {
                val module =
                    if (GeneratorExtension.isDefaultModule(table.module)) "modules" else table.module
                packageName =
                    if (packageName.endsWith(".$module") || packageName.contains(".$module.")) packageName else "$packageName.$module"
            }
            return if (ext.userModule && table.subModule.isNotBlank()) {
                "$packageName.${table.subModule}"
            } else {
                packageName
            }
        }


    val className
        get() =
            if (otherColumns.isEmpty()) "${table.className}Entity" else table.className


    val projectClassName
        get() = if (enable("projectClassName", true))
            table.className + shortProjectName
        else
            table.className

    val shortProjectName
        get() =
            projectName.replace("-", "").substring(
                0, if (projectName.length > 5) 5 else projectName.length
            ).capitalized()

    val entityName
        get() =
            if (otherColumns.isEmpty()) "${table.entityName}Entity" else table.entityName


    val projectEntityName get() = projectClassName.decapitalized()

    /**
     * 表名
     */
    val tableName: String
        get() = "${if (table.catalog.isNullOrBlank() || database.catalog.isNullOrBlank() || table.catalog == database.catalog) "" else "${table.catalog}."}${if (table.schema.isNullOrBlank() || database.schema.isNullOrBlank() || table.schema == database.schema) "" else "${table.schema}."}${table.tableName}"


    /**
     * 注释说明
     */
    val remarks: String
        get() {
            val comment = table.remarks.replace('?', ' ').trim('.').trim()
            return comment.ifBlank {
                ext.remarks
            }
        }


    /**
     * 主键
     */
    val primaryKeys: List<Column>
        get() {
            val primaryKeys = table.primaryKeys
            return if (primaryKeys.isEmpty()) {
                val primaryKey =
                    columns.find { it.columnName.equals(ext.primaryKeyName, true) }
                        ?: columns.find { it.remark.contains("主键") }
                if (primaryKey != null) {
                    listOf(primaryKey)
                } else {
                    columns
                }
            } else {
                primaryKeys
            }
        }

    /**
     * 主键
     */
    val primaryKey: Column
        get() =
            if (primaryKeys.size == 1) {
                primaryKeys[0]
            } else {
                throw GeneratorException("$tableName:没有单主键，${primaryKeys.joinToString()}")
            }


    val primaryKeyClassName: String
        get() {
            return if (primaryKeys.size == 1) {
                primaryKey.javaType.shortName
            } else {
                if (otherColumns.isEmpty()) table.className else "${
                    table.className
                }Key"
            }
        }

    /**
     * 主键
     */
    val primaryKeyName: String
        get() =
            if (primaryKeys.size == 1) {
                primaryKey.javaName
            } else {
                if (otherColumns.isEmpty())
                    table.entityName
                else
                    "${table.entityName}Key"
            }


    open val primaryKeyType: JavaType
        get() =
            if (primaryKeys.size == 1) {
                if (primaryKey.sequence.isNotBlank()) JavaType("java.lang.Long") else primaryKey.javaType
            } else {
                JavaType(
                    "$packageName.${modulePackage("Entity")}.${
                        if (isFullComposite) table.className
                        else "${table.className}Key"
                    }"
                )
            }


    val isFullComposite: Boolean get() = otherColumns.isEmpty()


    /**
     * 是否组合主键
     */
    val isCompositePrimaryKey: Boolean get() = primaryKeys.size > 1

    /**
     * 非主键字段
     */
    val otherColumns: List<Column> get() = columns.filter { !primaryKeys.contains(it) }

    val CompilationUnit.defaultColumns
        get() = otherColumns.filter {
            !it.version && !it.initializationString(this, false).isNullOrBlank()
        }

    /**
     * 字段
     */
    val columns: List<Column> get() = table.columns

    val pathName: String get() = table.pathName

    fun setting(key: String): Any? = ext.setting(key)

    fun setting(key: String, default: String): String {
        return ext.setting(key, default)
    }

    fun enable(key: String, default: Boolean = false): Boolean {
        return ext.enable(key, default)
    }

    fun modulePackage(name: String): String {
        val onePackage = enable("onePackage", true)
        return if (onePackage)
            table.entityName.lowercase(Locale.getDefault())
        else when (name) {
            "Domain" -> "domain"
            "QueryDsl" -> "querydsl"
            "Dao" -> "dao"
            "DataObject" -> "data"
            "Entity" -> "entity"
            "Properties" -> "properties"
            "Matcher" -> "matcher"
            "MethodInfo" -> "info"
            "Form" -> "form"
            "MixIn" -> "mixin"
            "Controller", "ControllerTest" -> "controller"
            "Service" -> "service"
            "ServiceImpl" -> "service.impl"
            "Repository" -> "repository"
            else -> table.entityName.lowercase(Locale.getDefault())
        }
    }

    operator fun GenUnit.unaryPlus() {
        units.add(this)
    }

    fun <T : GenUnit> add(unit: T): T {
        projectUnits.add(unit)
        return unit
    }

    operator fun get(name: String): GenUnit? {
        return projectUnits.find { it.name == name }
    }

    fun file(
        name: String,
        overwrite: Boolean = true,
        sourceSet: SourceSet = SourceSet.ROOT,
        directorySet: DirectorySet = DirectorySet.RESOURCES,
        apply: FileUnit.() -> Unit = { }
    ): FileUnit {
        return FileUnit(
            name = name,
            overwrite = overwrite,
            sourceSet = sourceSet,
            directorySet = directorySet
        ).apply(apply)
    }

    fun file(
        file: File,
        overwrite: Boolean = true,
        sourceSet: SourceSet = SourceSet.ROOT,
        directorySet: DirectorySet = DirectorySet.RESOURCES,
        apply: FileUnit.() -> Unit = { }
    ): FileUnit {
        return FileUnit(
            file = file,
            overwrite = overwrite,
            sourceSet = sourceSet,
            directorySet = directorySet
        ).apply(apply)
    }

    fun properties(
        name: String,
        overwrite: Boolean = false,
        sourceSet: SourceSet = SourceSet.MAIN,
        directorySet: DirectorySet = DirectorySet.RESOURCES,
        apply: PropertiesUnit.() -> Unit = { }
    ): PropertiesUnit {
        return PropertiesUnit(
            name = name,
            overwrite = overwrite,
            sourceSet = sourceSet,
            directorySet = directorySet
        ).apply(apply)
    }

    fun packageInfo(
        type: JavaType,
        overwrite: Boolean = false,
        sourceSet: SourceSet = SourceSet.MAIN,
        directorySet: DirectorySet = DirectorySet.JAVA,
        apply: PackageInfo.() -> Unit = { }
    ): PackageInfo {
        return PackageInfo(
            type = type,
            overwrite = overwrite,
            sourceSet = sourceSet,
            directorySet = directorySet
        ).apply(apply)
    }


    fun interfaze(
        type: JavaType,
        overwrite: Boolean = false,
        sourceSet: SourceSet = SourceSet.MAIN,
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        apply: Interface.() -> Unit = { }
    ): Interface {
        return Interface(
            type = type,
            overwrite = overwrite,
            sourceSet = sourceSet,
            visibility = visibility
        ).apply(apply)
    }

    fun clazz(
        type: JavaType,
        overwrite: Boolean = false,
        sourceSet: SourceSet = SourceSet.MAIN,
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        apply: TopLevelClass.() -> Unit = { }
    ): TopLevelClass {
        return TopLevelClass(
            type = type,
            overwrite = overwrite,
            sourceSet = sourceSet,
            visibility = visibility
        ).apply(apply)
    }


    fun enum(
        type: JavaType,
        overwrite: Boolean = false,
        sourceSet: SourceSet = SourceSet.MAIN,
        apply: TopLevelEnumeration.() -> Unit = { }
    ): TopLevelEnumeration {
        return TopLevelEnumeration(
            type = type,
            overwrite = overwrite,
            sourceSet = sourceSet
        ).apply(apply)
    }

    open fun content() {}

    open fun call() {
        units.clear()
        content()
        units.forEach { unit ->
            if (ext.replaceAll) unit.overwrite = true
            unit.writeTo(projectDir)
        }
    }

    open val projectDir: File get() = ext.projectDir

    fun run(table: Table) {
        this.table = table
        this.call()
    }

    fun setUp(ext: GeneratorExtension) {
        this.ext = ext
        JavaElement.INDENT = ext.indent
        setUp()
    }

    open fun setUp() {

    }

    fun preTearDown() {
        projectUnits.forEach { unit ->
            if (ext.replaceAll) unit.overwrite = true
            unit.writeTo(projectDir)
        }
    }

    open fun tearDown() {

    }


    fun Column.dicCodes(): DicCodes? {
        return if (isCodeField) {
            val prettyRemarks = prettyRemarks
            val codeTypeName = prettyRemarks.substringBefore('(')

            val dicCodes = DicCodes(
                codeType,
                codeTypeName,
                javaType.primitiveType ?: javaType
            )
            prettyRemarks.substringAfter('(').substringBeforeLast(')').trim('?', '.')
                .split(";").filter { it.isNotBlank() }
                .forEach { item: String ->
                    val code = item.substringBefore(":").trim().trim(',', '，').trim()
                    val name = item.substringAfter(":").trim().trim(',', '，').trim()
                    dicCodes.codes[code] = name
                }

            return dicCodes
        } else {
            null
        }
    }

    val Column.randomValue: Any
        get() = when {
            columnDef == null || "CURRENT_TIMESTAMP".equals(columnDef, true) ->
                when {
                    isCodeField && !asBoolean -> dicCodes()!!.codes.keys.first()
                    else ->
                        when (javaType) {
                            JavaType("java.math.BigDecimal") -> java.math.BigDecimal("1.0")
                            JavaType("java.sql.Timestamp") -> (System.currentTimeMillis())
                            JavaType.dateInstance -> (System.currentTimeMillis())
                            JavaType("java.sql.Date") -> (System.currentTimeMillis())
                            JavaType("java.sql.Time") -> (System.currentTimeMillis())
                            JavaType("java.time.LocalDate") -> (System.currentTimeMillis())
                            JavaType("java.time.LocalDateTime") -> (System.currentTimeMillis())
                            JavaType.booleanWrapper -> false
                            JavaType.doubleWrapper -> 1.0
                            JavaType.longWrapper -> 1L
                            JavaType.intWrapper -> 1
                            JavaType.stringInstance -> remark.replace("\\", "\\\\")
                            else -> 1
                        }
                }

            else -> columnDef!!
        }

    fun Column.randomValueToSet(unit: CompilationUnit? = null): String {
        val initializationString = initializationString(unit)
        return when {
            initializationString.isNullOrBlank() -> {
                when {
                    isCodeField && !asBoolean -> {
                        val value = dicCodes()!!.codes.keys.first()
                        if (JavaType.stringInstance == javaType) "\"$value\"" else if (JavaType.charWrapper == javaType) "(char) $value" else "$value"
                    }

                    else -> when (javaType) {
                        JavaType("java.sql.Timestamp") -> {
                            unit?.import("java.sql.Timestamp")
                            "new Timestamp(System.currentTimeMillis())"
                        }

                        JavaType.dateInstance -> {
                            unit?.import("java.util.Date")
                            "new Date(System.currentTimeMillis())"
                        }

                        JavaType("java.sql.Date") -> {
                            unit?.import("java.sql.Date")
                            "new Date(System.currentTimeMillis())"
                        }

                        JavaType("java.sql.Time") -> {
                            unit?.import("java.sql.Time")
                            "new Time(System.currentTimeMillis())"
                        }

                        JavaType("java.time.LocalDate") -> {
                            unit?.import("java.time.LocalDate")
                            "LocalDate.now()"
                        }

                        JavaType("java.time.LocalDateTime") -> {
                            unit?.import("java.time.LocalDateTime")
                            "LocalDateTime.now()"
                        }

                        JavaType("java.math.BigDecimal") -> {
                            unit?.import("java.math.BigDecimal")
                            "new BigDecimal(\"1\")"
                        }

                        JavaType.booleanWrapper -> "false"
                        JavaType.doubleWrapper -> "1.0"
                        JavaType.longWrapper -> "1L"
                        JavaType.intWrapper -> "1"
                        JavaType.charWrapper -> "(char) 1"
                        JavaType.shortWrapper -> "new Short(\"1\")"
                        JavaType.byteWrapper -> "new Byte(\"1\")"
                        JavaType("byte[]") -> "new byte[0]"
                        JavaType.stringInstance -> "\"${remark.replace("\\", "\\\\")}\""
                        else -> "1"
                    }
                }
            }

            else -> initializationString
        }
    }

    fun Column.initializationString(unit: CompilationUnit?, importType: Boolean = true): String? {
        return if (columnDef != null) {
            when (javaType) {
                JavaType.booleanWrapper -> BooleanUtil.toBoolean(columnDef)?.toString()
                JavaType.longWrapper -> "${columnDef}L"
                JavaType.doubleWrapper -> "${columnDef}D"
                JavaType.floatWrapper -> "${columnDef}F"
                JavaType("java.math.BigDecimal") -> {
                    if (importType)
                        unit?.import("java.math.BigDecimal")
                    "new BigDecimal(\"$columnDef\")"
                }

                JavaType.stringInstance -> "\"$columnDef\""
                else -> {
                    if ("CURRENT_TIMESTAMP".equals(columnDef, true) || "SYSDATE".equals(
                            columnDef,
                            true
                        )
                    ) {
                        when (javaType) {
                            JavaType("java.sql.Timestamp") -> {
                                if (importType)
                                    unit?.import("java.sql.Timestamp")
                                "new Timestamp(System.currentTimeMillis())"
                            }

                            JavaType.dateInstance -> {
                                if (importType)
                                    unit?.import("java.util.Date")
                                "new Date(System.currentTimeMillis())"
                            }

                            JavaType("java.sql.Date") -> {
                                if (importType)
                                    unit?.import("java.sql.Date")
                                "new Date(System.currentTimeMillis())"
                            }

                            JavaType("java.sql.Time") -> {
                                if (importType)
                                    unit?.import("java.sql.Time")
                                "new Time(System.currentTimeMillis())"
                            }

                            JavaType("java.time.LocalDate") -> {
                                if (importType)
                                    unit?.import("java.time.LocalDate")
                                "LocalDate.now()"
                            }

                            JavaType("java.time.LocalDateTime") -> {
                                if (importType)
                                    unit?.import("java.time.LocalDateTime")
                                "LocalDateTime.now()"
                            }

                            else -> columnDef
                        }
                    } else {
                        columnDef
                    }
                }
            }
        } else {
            null
        }
    }


    companion object {

        fun enumClassName(codeType: String): String {
            return codeType.split("_").joinToString("") {
                if (codeType.matches(Regex(".*[a-z].*")))
                    it.capitalized()
                else
                    it.lowercase(Locale.getDefault())
                        .capitalized()
            }
        }
    }

}
