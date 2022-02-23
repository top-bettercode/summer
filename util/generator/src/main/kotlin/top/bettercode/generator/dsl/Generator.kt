package top.bettercode.generator.dsl

import top.bettercode.generator.GeneratorException
import top.bettercode.generator.GeneratorExtension
import top.bettercode.generator.JDBCConnectionConfiguration
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.database.entity.Table
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.PrimitiveTypeWrapper
import top.bettercode.generator.dom.java.element.*
import java.io.File

/**
 * 模板基类
 */
open class Generator {

    private val units: MutableList<GenUnit> = mutableListOf()

    val Table.supportSoftDelete: Boolean
        get() = supportSoftDelete(extension)

    val Column.jsonViewIgnored: Boolean
        get() = jsonViewIgnored(extension)

    val Column.isSoftDelete: Boolean
        get() = isSoftDelete(extension)

    val Column.defaultRemarks: String
        get() = defaultDesc.replace("DEFAULT ", "默认值：")

    val Column.docRemark: String
        get() = "${
            (if (remarks.isBlank()) "" else (if (isSoftDelete) remarks.split(Regex("[:：,， (（]"))[0] else remarks.replace(
                "@",
                "\\@"
            )))
        }${if (columnDef.isNullOrBlank() || isSoftDelete) "" else " 默认值：${columnDef}"}"

    val Column.paramRemark: String
        get() = if (docRemark.isBlank()) "" else "@param $javaName $docRemark"


    val Column.returnRemark: String
        get() {
            val remark = docRemark.replace(Regex(" ?([:;/]) ?"), " $1 ")
            return if (remark.isBlank()) "" else "@return $remark"
        }


    lateinit var table: Table
    lateinit var extension: GeneratorExtension

    val settings: Map<String, String> get() = extension.settings

    val datasource: JDBCConnectionConfiguration get() = extension.datasources[table.module]!!

    val projectName: String get() = extension.projectName


    val basePackageName: String
        get() =
            (if (extension.projectPackage) "${extension.packageName}.$projectName" else extension.packageName)


    val packageName: String
        get() {
            var packageName = basePackageName
            if (settings["no-modules"] == null)
                packageName = "$packageName.${table.module}"
            return if (extension.userModule && table.subModule.isNotBlank()) {
                "$packageName.${table.subModule}"
            } else {
                packageName
            }
        }


    val className
        get() =
            if (otherColumns.isEmpty()) "${table.className(extension)}Entity" else table.className(
                extension
            )


    val projectClassName
        get() =
            if (enable(
                    "projectClassName",
                    true
                )
            ) table.className(extension) + projectName.substring(
                0, if (projectName.length > 5) 5 else projectName.length
            ).capitalize() else table.className(extension)


    val entityName
        get() =
            if (otherColumns.isEmpty()) "${table.entityName(extension)}Entity" else table.entityName(
                extension
            )


    val projectEntityName get() = projectClassName.decapitalize()

    /**
     * 表名
     */
    val tableName: String
        get() =
            if (table.schema.isNullOrBlank() || table.schema == datasource.schema) table.tableName else "${table.schema}.${table.tableName}"


    /**
     * 注释说明
     */
    val remarks: String
        get() {
            val comment = table.remarks
            return comment.ifBlank {
                extension.remarks
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
                    columns.find { it.columnName.equals(extension.primaryKeyName, true) }
                        ?: columns.find { it.remarks.contains("主键") }
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
                if (otherColumns.isEmpty()) table.className(extension) else "${
                    table.className(
                        extension
                    )
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
                    table.entityName(extension)
                else
                    "${table.entityName(extension)}Key"
            }


    open val primaryKeyType: JavaType
        get() =
            if (primaryKeys.size == 1) {
                if (primaryKey.sequence.isNotBlank()) JavaType("java.lang.Long") else primaryKey.javaType
            } else {
                JavaType(
                    "$packageName.${modulePackage("Entity")}.${
                        if (isFullComposite) table.className(extension)
                        else "${table.className(extension)}Key"
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

    /**
     * 字段
     */
    val columns: List<Column> get() = table.columns

    val pathName: String get() = table.pathName(extension)

    fun setting(key: String): Any? = settings[key]

    fun setting(key: String, default: String): String {
        return settings[key] ?: return default
    }

    fun enable(key: String, default: Boolean = true): Boolean {
        return setting(key, default.toString()) == "true"
    }

    fun modulePackage(name: String): String {
        val onePackage = enable("onePackage", true)
        return if (onePackage)
            table.entityName(extension).toLowerCase()
        else when (name) {
            "Domain" -> "domain"
            "QueryDsl" -> "querydsl"
            "Dao" -> "dao"
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
            else -> table.entityName(extension).toLowerCase()
        }
    }

    fun addUnit(unit: GenUnit) {
        units.add(unit)
    }

    val GenUnit.file: File
        get() {
            var dir =
                if (isTestFile) extension.dir.replace("src/main/", "src/test/") else extension.dir
            dir = if (isResourcesFile) dir.replace("java", "resources") else dir
            val file = File(name)
            return if (file.isAbsolute) file else File(
                File(extension.basePath, dir),
                name
            )
        }

    fun selfOutput(
        name: String,
        canCover: Boolean = false,
        isResourcesFile: Boolean = false,
        isTestFile: Boolean = false,
        unit: SelfOutputUnit.() -> Unit
    ) {
        val value = SelfOutputUnit(
            name = name,
            canCover = canCover,
            isResourcesFile = isResourcesFile,
            isTestFile = isTestFile
        )
        unit(value)
        addUnit(value)
    }

    fun file(
        name: String,
        canCover: Boolean = false,
        isResourcesFile: Boolean = false,
        isTestFile: Boolean = false,
        unit: FileUnit.() -> Unit
    ) {
        val value = FileUnit(
            name = name,
            canCover = canCover,
            isResourcesFile = isResourcesFile,
            isTestFile = isTestFile
        )
        unit(value)
        addUnit(value)
    }

    fun packageInfo(
        type: JavaType,
        canCover: Boolean = false,
        isResourcesFile: Boolean = false,
        isTestFile: Boolean = false,
        unit: PackageInfo.() -> Unit
    ) {
        val value = PackageInfo(
            type = type,
            canCover = canCover,
            isResourcesFile = isResourcesFile,
            isTestFile = isTestFile
        )
        unit(value)
        addUnit(value)
    }


    fun interfaze(
        type: JavaType,
        canCover: Boolean = false,
        isResourcesFile: Boolean = false,
        isTestFile: Boolean = false,
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        interfaze: Interface.() -> Unit
    ) {
        val value = Interface(
            type = type,
            canCover = canCover,
            isResourcesFile = isResourcesFile,
            isTestFile = isTestFile
        )
        value.visibility = visibility
        interfaze(value)
        addUnit(value)
    }

    fun clazz(
        type: JavaType,
        canCover: Boolean = false,
        isResourcesFile: Boolean = false,
        isTestFile: Boolean = false,
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        clazz: TopLevelClass.() -> Unit
    ) {
        val value = TopLevelClass(
            type = type,
            canCover = canCover,
            isResourcesFile = isResourcesFile,
            isTestFile = isTestFile
        )
        value.visibility = visibility
        clazz(value)
        addUnit(value)
    }

    fun enum(
        type: JavaType,
        canCover: Boolean = false,
        isResourcesFile: Boolean = false,
        isTestFile: Boolean = false,
        enum: TopLevelEnumeration.() -> Unit
    ) {
        val value = TopLevelEnumeration(
            type = type,
            canCover = canCover,
            isResourcesFile = isResourcesFile,
            isTestFile = isTestFile
        )
        enum(value)
        addUnit(value)
    }


    open fun content() {}

    open fun call() {
        JavaElement.indent = extension.indent
        units.clear()
        content()
        units.filter { it !is SelfOutputUnit }.forEach { unit ->
            val destFile = unit.file

            if (extension.delete)
                if (destFile.delete()) {
                    println("删除：${destFile.absolutePath.substringAfter(extension.basePath.absolutePath + File.separator)}")
                }

            if (!destFile.exists() || !((!extension.replaceAll && !unit.canCover) || destFile.readLines()
                    .any { it.contains("[[Don't cover]]") })
            ) {
                destFile.parentFile.mkdirs()

                println(
                    "${if (destFile.exists()) "覆盖" else "生成"}：${
                        destFile.absolutePath.substringAfter(
                            (extension.rootPath ?: extension.basePath).absolutePath + File.separator
                        )
                    }"
                )
                unit.output(destFile.printWriter())
            }
        }
    }

    fun run(table: Table) {
        this.table = table
        this.call()
    }

    fun setUp(extension: GeneratorExtension) {
        this.extension = extension
        setUp()
    }

    open fun setUp() {

    }

    open fun tearDown() {

    }


    fun Column.dicCodes(extension: GeneratorExtension): DicCodes? {
        return if (isCodeField) {
            val codeType = if (columnName.contains("_") || extension.commonCodeTypes.any {
                    it.equals(
                        columnName,
                        true
                    )
                }) javaName else (className + javaName.capitalize()).decapitalize()
            val prettyRemarks = prettyRemarks
            val codeTypeName = prettyRemarks.substringBefore('(')

            val dicCodes = DicCodes(
                codeType,
                codeTypeName,
                JavaType.stringInstance != javaType
            )
            prettyRemarks.substringAfter('(').substringBeforeLast(')')
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
            columnDef.isNullOrBlank() || "CURRENT_TIMESTAMP".equals(columnDef, true) ->
                when {
                    isCodeField -> dicCodes(extension)!!.codes.keys.first()
                    else ->
                        when (javaType) {
                            JavaType("java.math.BigDecimal") -> java.math.BigDecimal("1.0")
                            JavaType("java.sql.Timestamp") -> (System.currentTimeMillis())
                            JavaType.dateInstance -> (System.currentTimeMillis())
                            JavaType("java.sql.Date") -> (System.currentTimeMillis())
                            JavaType("java.sql.Time") -> (System.currentTimeMillis())
                            JavaType("java.time.LocalDate") -> (System.currentTimeMillis())
                            JavaType("java.time.LocalDateTime") -> (System.currentTimeMillis())
                            PrimitiveTypeWrapper.booleanInstance -> true
                            PrimitiveTypeWrapper.doubleInstance -> 1.0
                            PrimitiveTypeWrapper.longInstance -> 1L
                            PrimitiveTypeWrapper.integerInstance -> 1
                            JavaType.stringInstance -> remarks.replace("\"", "\\\"")
                            else -> 1
                        }
                }
            else -> columnDef!!
        }

    val Column.randomValueToSet: String
        get() =
            when {
                initializationString.isNullOrBlank() || "CURRENT_TIMESTAMP".equals(
                    columnDef,
                    true
                ) -> {
                    when {
                        isCodeField -> {
                            val value = dicCodes(extension)!!.codes.keys.first()
                            if (JavaType.stringInstance == javaType) "\"$value\"" else "$value"
                        }
                        else -> when (javaType) {
                            JavaType("java.math.BigDecimal") -> "new java.math.BigDecimal(\"1.0\")"
                            JavaType("java.sql.Timestamp") -> "new java.sql.Timestamp(System.currentTimeMillis())"
                            JavaType.dateInstance -> "new java.util.Date(System.currentTimeMillis())"
                            JavaType("java.sql.Date") -> "new java.sql.Date(System.currentTimeMillis())"
                            JavaType("java.sql.Time") -> "new java.sql.Time(System.currentTimeMillis())"
                            JavaType("java.time.LocalDate") -> "java.time.LocalDate.now()"
                            JavaType("java.time.LocalDateTime") -> "java.time.LocalDateTime.now()"
                            PrimitiveTypeWrapper.booleanInstance -> "true"
                            PrimitiveTypeWrapper.doubleInstance -> "1.0"
                            PrimitiveTypeWrapper.longInstance -> "1L"
                            PrimitiveTypeWrapper.integerInstance -> "1"
                            PrimitiveTypeWrapper.shortInstance -> "new Short(\"1\")"
                            PrimitiveTypeWrapper.byteInstance -> "new Byte(\"1\")"
                            JavaType("byte[]") -> "new byte[0]"
                            JavaType.stringInstance -> "\"${remarks.replace("\"", "\\\"")}\""
                            else -> "1"
                        }
                    }
                }
                else -> initializationString!!
            }

    val Column.testId: Any
        get() = when (javaType) {
            JavaType.stringInstance -> "\"1\""
            PrimitiveTypeWrapper.longInstance -> "1L"
            PrimitiveTypeWrapper.integerInstance -> 1
            else -> 1
        }

    private val Column.initializationString
        get() = if (!columnDef.isNullOrBlank()) {
            when (javaType.shortName) {
                "Boolean" -> toBoolean(columnDef).toString()
                "Long" -> "${columnDef}L"
                "Double" -> "${columnDef}D"
                "Float" -> "${columnDef}F"
                "BigDecimal" -> "new BigDecimal($columnDef)"
                "String" -> "\"$columnDef\""
                else -> columnDef
            }
        } else {
            columnDef
        }


    fun Column.setValue(value: String): String {
        return "\"null\".equals($value) ? null : " + when (javaType.shortName) {
            "Boolean" -> "Boolean.valueOf($value)"
            "Integer" -> "Integer.valueOf($value)"
            "Long" -> "Long.valueOf($value)"
            "Double" -> "Double.valueOf($value)"
            "Float" -> "Float.valueOf($value)"
            "BigDecimal" -> "new BigDecimal($value)"
            else -> value
        }
    }


    companion object {
        fun toBoolean(obj: Any?): Boolean {
            when (obj) {
                is Boolean -> return obj
                is String -> {
                    if (obj.equals("true", true)) {
                        return true
                    }
                    when (obj.length) {
                        1 -> {
                            val ch0 = obj[0]
                            if (ch0 == 'y' || ch0 == 'Y' ||
                                ch0 == 't' || ch0 == 'T' || ch0 == '1'
                            ) {
                                return true
                            }
                            if (ch0 == 'n' || ch0 == 'N' ||
                                ch0 == 'f' || ch0 == 'F' || ch0 == '0'
                            ) {
                                return false
                            }
                        }
                        2 -> {
                            val ch0 = obj[0]
                            val ch1 = obj[1]
                            if ((ch0 == 'o' || ch0 == 'O') && (ch1 == 'n' || ch1 == 'N')) {
                                return true
                            }
                            if ((ch0 == 'n' || ch0 == 'N') && (ch1 == 'o' || ch1 == 'O')) {
                                return false
                            }
                        }
                        3 -> {
                            val ch0 = obj[0]
                            val ch1 = obj[1]
                            val ch2 = obj[2]
                            if ((ch0 == 'y' || ch0 == 'Y') &&
                                (ch1 == 'e' || ch1 == 'E') &&
                                (ch2 == 's' || ch2 == 'S')
                            ) {
                                return true
                            }
                            if ((ch0 == 'o' || ch0 == 'O') &&
                                (ch1 == 'f' || ch1 == 'F') &&
                                (ch2 == 'f' || ch2 == 'F')
                            ) {
                                return false
                            }
                        }
                        4 -> {
                            val ch0 = obj[0]
                            val ch1 = obj[1]
                            val ch2 = obj[2]
                            val ch3 = obj[3]
                            if ((ch0 == 't' || ch0 == 'T') &&
                                (ch1 == 'r' || ch1 == 'R') &&
                                (ch2 == 'u' || ch2 == 'U') &&
                                (ch3 == 'e' || ch3 == 'E')
                            ) {
                                return true
                            }
                        }
                        5 -> {
                            val ch0 = obj[0]
                            val ch1 = obj[1]
                            val ch2 = obj[2]
                            val ch3 = obj[3]
                            val ch4 = obj[4]
                            if ((ch0 == 'f' || ch0 == 'F') &&
                                (ch1 == 'a' || ch1 == 'A') &&
                                (ch2 == 'l' || ch2 == 'L') &&
                                (ch3 == 's' || ch3 == 'S') &&
                                (ch4 == 'e' || ch4 == 'E')
                            ) {
                                return false
                            }
                        }
                        else -> {
                        }
                    }
                }
                is Number -> return obj.toInt() > 0
            }

            return false
        }

    }

}
