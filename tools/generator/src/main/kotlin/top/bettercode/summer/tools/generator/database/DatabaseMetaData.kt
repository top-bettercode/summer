package top.bettercode.summer.tools.generator.database

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.generator.DatabaseConfiguration
import top.bettercode.summer.tools.generator.DatabaseDriver
import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.database.entity.Indexed
import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.puml.PumlConverter
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.util.*


/**
 * 数据库MetaData
 *
 * @author Peter Wu
 */


@Suppress("ConvertTryFinallyToUseCall")
class DatabaseMetaData(
        private val datasource: DatabaseConfiguration
) : AutoCloseable {
    private val log: Logger = LoggerFactory.getLogger(DatabaseMetaData::class.java)
    private var databaseMetaData: java.sql.DatabaseMetaData
    private var canReadIndexed = true

    init {
        val connection = DriverManager.getConnection(datasource.url, datasource.properties)
        this.databaseMetaData = connection.metaData
    }

    private fun reConnect() {
        close()
        val connection = DriverManager.getConnection(datasource.url, datasource.properties)
        this.databaseMetaData = connection.metaData
    }

    override fun close() {
        try {
            databaseMetaData.connection.close()
        } catch (e: Exception) {
            log.error("关闭数据库连接出错:${e.message}")
        }
    }


    private fun String.current(call: (String?, String) -> Unit) {
        var curentSchema = datasource.schema
        var curentTableName = this
        if (this.contains('.')) {
            val names = this.split('.')
            curentSchema = names[0]
            curentTableName = names[1]
        }
        call(curentSchema, curentTableName)
    }

    fun <T> ResultSet.map(rs: ResultSet.() -> T): List<T> {
        try {
            val list = mutableListOf<T>()
            while (next()) {
                list.add(rs(this))
            }
            return list
        } finally {
            close()
        }
    }

    fun <T> ResultSet.use(rs: ResultSet.() -> T): List<T> {
        try {
            val list = mutableListOf<T>()
            while (next()) {
                list.add(rs(this))
            }
            return list
        } finally {
            close()
        }
    }

    /**
     * 所有数据表
     * @return 数据表名
     */
    fun tableNames(): List<String> {
        return databaseMetaData.getTables(datasource.catalog, datasource.schema, null, arrayOf("TABLE"))
                .map { getString("TABLE_NAME") }.sortedBy { it }
    }

    /**
     * @param tableName 表名
     * @return 数据表
     */
    fun table(tableName: String, call: (Table) -> Unit = {}): Table? {
        log.warn("查询：$tableName 表数据结构")
        var table: Table? = null

        val databaseProductName = databaseMetaData.databaseProductName
        tableName.current { curentSchema, curentTableName ->
            val tables =
                    databaseMetaData.getTables(datasource.catalog, curentSchema, curentTableName, null)
            table = tables.toTable(call)
        }
        if (table == null) {
            log.error("未在${databaseProductName}数据库(${tableNames().joinToString()})中找到${tableName}表")
        }
        return table
    }

    private fun ResultSet.toTable(call: (Table) -> Unit = {}): Table? {
        try {
            while (next()) {
                if (datasource.debug)
                    debug("table", this.metaData)

                val schema = getString("TABLE_SCHEM")
                val name = getString("TABLE_NAME")
                val tableCat = getString("TABLE_CAT")
                val tableType = getString("TABLE_TYPE")
                val remarks = getString("REMARKS")?.replace("[\t\n\r]".toRegex(), "")?.trim() ?: ""
                val engine: String = getEngine(name)
                val collate: String = getCollate(name)

                val columns = columns(name)
                fixImportedKeys(schema, name, columns)
                fixColumns(name, columns)
                var primaryKeyNames: MutableList<String>
                var indexes: MutableList<Indexed>
                if (canReadIndexed) {
                    try {
                        primaryKeyNames = primaryKeyNames(name)
                        indexes = if (datasource.queryIndex)
                            indexes(name)
                        else
                            mutableListOf()
                    } catch (e: Exception) {
                        log.error("查询索引出错:${e.message}")
                        reConnect()
                        canReadIndexed = false
                        datasource.queryIndex = false
                        primaryKeyNames = mutableListOf()
                        indexes = mutableListOf()
                    }
                } else {
                    primaryKeyNames = mutableListOf()
                    indexes = mutableListOf()
                }
                val table = Table(
                        productName = databaseMetaData.databaseProductName,
                        catalog = tableCat ?: datasource.catalog,
                        schema = schema ?: datasource.schema,
                        tableName = name,
                        tableType = tableType,
                        remarks = remarks,
                        primaryKeyNames = primaryKeyNames,
                        indexes = indexes,
                        pumlColumns = columns.toMutableList(),
                        engine = engine,
                        collate = collate
                )
                call(table)
                return table
            }
            return null
        } finally {
            close()
        }
    }

    fun getSchemaDefaultCollate(): String {
        val databaseDriver =
                DatabaseDriver.fromJdbcUrl(databaseMetaData.url)
        var collate = ""
        if (arrayOf(
                        DatabaseDriver.MYSQL,
                        DatabaseDriver.MARIADB
                ).contains(
                        databaseDriver
                )
        ) {
            try {
                val prepareStatement =
                        databaseMetaData.connection.prepareStatement("SELECT DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA where schema_name=?")
                prepareStatement.setString(1, datasource.schema)
                prepareStatement.queryTimeout = 5
                prepareStatement.executeQuery().map {
                    collate = getString("DEFAULT_COLLATION_NAME")
                    return@map
                }
            } catch (e: Exception) {
                log.error("查询库编码排序出错:${e.message}")
            }
        }
        return collate
    }

    private fun getCollate(tableName: String): String {
        val databaseDriver =
                DatabaseDriver.fromJdbcUrl(databaseMetaData.url)
        var collate = ""
        if (arrayOf(
                        DatabaseDriver.MYSQL,
                        DatabaseDriver.MARIADB
                ).contains(
                        databaseDriver
                )
        ) {
            try {
                val prepareStatement =
                        databaseMetaData.connection.prepareStatement("SELECT table_collation FROM information_schema.TABLES where table_name = ? and table_schema=?")
                prepareStatement.setString(1, tableName)
                prepareStatement.setString(2, datasource.schema)
                prepareStatement.queryTimeout = 5
                prepareStatement.executeQuery().map {
                    collate = getString("table_collation")
                    return@map
                }
            } catch (e: Exception) {
                log.error("查询表编码排序出错:${e.message}")
            }
        }
        return collate
    }

    private fun getEngine(tableName: String): String {
        val databaseDriver =
                DatabaseDriver.fromJdbcUrl(databaseMetaData.url)
        var engine = ""
        if (arrayOf(
                        DatabaseDriver.MYSQL,
                        DatabaseDriver.MARIADB,
                        DatabaseDriver.H2
                ).contains(
                        databaseDriver
                )
        ) {
            try {
                val prepareStatement =
                        databaseMetaData.connection.prepareStatement("SHOW TABLE STATUS LIKE ?")
                prepareStatement.setString(1, tableName)
                prepareStatement.queryTimeout = 5
                prepareStatement.executeQuery().map {
                    engine = getString("Engine")
                    return@map
                }
            } catch (e: Exception) {
                log.error("查询表引擎出错:${e.message}")
            }
        }
        return engine
    }

    private fun fixColumns(tableName: String, columns: MutableList<Column>) {
        val databaseDriver =
                DatabaseDriver.fromJdbcUrl(databaseMetaData.url)
        if (arrayOf(
                        DatabaseDriver.MYSQL,
                        DatabaseDriver.MARIADB,
                        DatabaseDriver.H2
                ).contains(
                        databaseDriver
                )
        ) {
            val quoteMark = "`"
            try {
                val prepareStatement =
                        databaseMetaData.connection.prepareStatement("SHOW COLUMNS FROM $quoteMark$tableName$quoteMark")
                prepareStatement.queryTimeout = 5
                prepareStatement.executeQuery().map {
                    val find = columns.find { it.columnName == getString(1) }
                    if (find != null) {
                        if (datasource.debug)
                            debug("column", this.metaData)
                        try {
                            find.extra = getString(6)
                            if (find.extra.contains("AUTO_INCREMENT", true)) {
                                find.autoIncrement = true
                                find.extra = find.extra.replace("AUTO_INCREMENT", "", true)
                                        .replace("  ", " ", true).trim()
                            }
                        } catch (ignore: Exception) {
                        }
                        val type = getString(2)
                        val (columnSize, decimalDigits) = PumlConverter.parseType(type)
                        find.typeName = type.substringBefore('(').uppercase(Locale.getDefault())
                        find.columnSize = columnSize
                        find.decimalDigits = decimalDigits
                    }
                }
            } catch (e: Exception) {
                log.error("\"SHOW COLUMNS FROM $tableName\"出错:${e.message}")
            }
        }
    }

    /**
     * 数据字段
     * @param tableName 表名
     * @return 字段集
     */
    private fun columns(tableName: String, vararg columnNames: String): MutableList<Column> {
        val columns = mutableListOf<Column>()
        tableName.current { curentSchema, curentTableName ->
            if (columnNames.isEmpty()) {
                databaseMetaData.getColumns(datasource.catalog, curentSchema, curentTableName, null)
                        .map {
                            fillColumn(columns)
                        }
            } else {
                columnNames.forEach {
                    databaseMetaData.getColumns(
                            datasource.catalog,
                            curentSchema,
                            curentTableName,
                            it
                    ).map {
                        fillColumn(columns)
                    }
                }
            }
        }
        return columns
    }

    private fun fixImportedKeys(
            curentSchema: String?,
            curentTableName: String,
            columns: MutableList<Column>
    ) {
        databaseMetaData.getImportedKeys(datasource.catalog, curentSchema, curentTableName).map {
            val find = columns.find { it.columnName == getString("FKCOLUMN_NAME") }!!
            find.isForeignKey = true
            find.pktableName = getString("PKTABLE_NAME")
            find.pkcolumnName = getString("PKCOLUMN_NAME")
        }
    }

    private fun ResultSet.fillColumn(columns: MutableList<Column>) {
        var supportsIsAutoIncrement = false
        var supportsIsGeneratedColumn = false

        val rsmd = metaData
        val colCount = rsmd.columnCount
        for (i in 1..colCount) {
            if ("IS_AUTOINCREMENT" == rsmd.getColumnName(i)) {
                supportsIsAutoIncrement = true
            }
            if ("IS_GENERATEDCOLUMN" == rsmd.getColumnName(i)) {
                supportsIsGeneratedColumn = true
            }
        }
        if (datasource.debug)
            debug("column", rsmd)
        val columnName = getString("COLUMN_NAME")
        val typeName = getString("TYPE_NAME").substringBefore("(")
        val dataType = getInt("DATA_TYPE")
        val nullable = getInt("NULLABLE") == 1
        val decimalDigits = getInt("DECIMAL_DIGITS")
        val def = getString("COLUMN_DEF")
        val columnDef =
                if (def != null && def.isNotEmpty() && def.isBlank()) " " else def?.trim()
        val columnSize = getInt("COLUMN_SIZE")
        val remarks = getString("REMARKS")?.replace("[\t\n\r]".toRegex(), "")?.trim()
                ?: ""
        val tableCat = getString("TABLE_CAT") ?: datasource.catalog
        val tableSchem = getString("TABLE_SCHEM") ?: datasource.schema
        val column = Column(
                tableCat = tableCat,
                tableSchem = tableSchem,
                columnName = columnName,
                typeName = typeName,
                dataType = dataType,
                decimalDigits = decimalDigits,
                columnSize = columnSize,
                remarks = remarks,
                nullable = nullable,
                columnDef = columnDef,
                unsigned = typeName.contains("UNSIGNED", true)
        )
        if (supportsIsAutoIncrement) {
            column.autoIncrement = "YES" == getString("IS_AUTOINCREMENT")
        }
        if (supportsIsGeneratedColumn) {
            column.generatedColumn = "YES" == getString("IS_GENERATEDCOLUMN")
        }
        columns.add(column)
    }

    private fun ResultSet.debug(name: String, rsmd: ResultSetMetaData) {
        val colCount = rsmd.columnCount
        log.warn("-----------------------------------------------------")
        for (i in 1..colCount) {
            log.warn("::$name::${metaData.getColumnName(i)}:${getString(i)}")
        }
        log.warn("-----------------------------------------------------")
    }

    /**
     * 获取表主键
     * @param tableName 表名
     * @return 主键字段名
     */
    private fun primaryKeyNames(tableName: String): MutableList<String> {
        val primaryKeys = mutableListOf<String>()
        tableName.current { curentSchema, curentTableName ->
            databaseMetaData.getPrimaryKeys(datasource.catalog, curentSchema, curentTableName).map {
                primaryKeys.add(getString("COLUMN_NAME"))
            }
        }

        return primaryKeys
    }

    private fun indexes(tableName: String): MutableList<Indexed> {
        val indexes = mutableListOf<Indexed>()
        tableName.current { curentSchema, curentTableName ->
            databaseMetaData.getIndexInfo(
                    datasource.catalog,
                    curentSchema,
                    curentTableName,
                    false,
                    true
            )
                    .map {
                        val indexName = getString("INDEX_NAME")
                        if (!indexName.isNullOrBlank() && !"PRIMARY".equals(indexName, true)) {
                            var indexed = indexes.find { it.name(datasource.fixTableName(tableName)) == indexName }
                            if (indexed == null) {
                                indexed = Indexed(unique = !getBoolean("NON_UNIQUE"), indexName = indexName)
                                indexes.add(indexed)
                            }
                            indexed.columnName.add(getString("COLUMN_NAME"))
                        }
                    }
        }
        return indexes
    }
}