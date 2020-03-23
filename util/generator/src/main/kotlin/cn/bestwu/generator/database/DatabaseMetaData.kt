package cn.bestwu.generator.database

import cn.bestwu.generator.DatabaseDriver
import cn.bestwu.generator.JDBCConnectionConfiguration
import cn.bestwu.generator.database.domain.Column
import cn.bestwu.generator.database.domain.Indexed
import cn.bestwu.generator.database.domain.Table
import cn.bestwu.generator.puml.PumlConverter
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.ResultSetMetaData

/**
 * 数据库MetaData
 *
 * @author Peter Wu
 */
fun ResultSet.use(rs: ResultSet.() -> Unit) {
    try {
        while (next()) {
            rs(this)
        }
    } finally {
        close()
    }
}

class DatabaseMetaData(private val datasource: JDBCConnectionConfiguration, private val debug: Boolean = false) : AutoCloseable {

    private var metaData: java.sql.DatabaseMetaData
    private val catalog: String?
    private var canReadIndexed = true

    init {
        val connection = DriverManager.getConnection(datasource.url, datasource.properties)
        this.metaData = connection.metaData
        catalog = datasource.catalog
    }

    private fun reConnect() {
        close()
        val connection = DriverManager.getConnection(datasource.url, datasource.properties)
        this.metaData = connection.metaData
    }

    override fun close() {
        try {
            metaData.connection.close()
        } catch (e: Exception) {
            System.err.println("关闭数据库连接出错:${e.message}")
        }
    }

    /**
     * 所有数据表
     * @return 数据表名
     */
    fun tableNames(): List<String> {
        val tableNames = mutableListOf<String>()
        metaData.getTables(datasource.catalog, datasource.schema, null, null).use { tableNames.add(getString("TABLE_NAME")) }
        return tableNames
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

    /**
     * 所有数据表
     * @param tableName 表名
     * @return 数据表
     */
    fun table(tableName: String): Table? {
        println("查询：$tableName 表数据结构")
        var table: Table? = null

        val databaseProductName = metaData.databaseProductName
        tableName.current { curentSchema, curentTableName ->
            val columns = columns(tableName)
            fixImportedKeys(curentSchema, curentTableName, columns)
            fixColumns(tableName, columns)
            var primaryKeyNames: MutableList<String>
            var indexes: MutableList<Indexed>
            if (canReadIndexed) {
                try {
                    primaryKeyNames = primaryKeyNames(tableName)
                    indexes = indexes(tableName)
                } catch (e: Exception) {
                    System.err.println("查询索引出错:${e.message}")
                    reConnect()
                    canReadIndexed = false
                    primaryKeyNames = mutableListOf()
                    indexes = mutableListOf()
                }
            } else {
                primaryKeyNames = mutableListOf()
                indexes = mutableListOf()
            }
            metaData.getTables(catalog, curentSchema, curentTableName, null).use {
                table = Table(productName = databaseProductName, catalog = catalog, schema = curentSchema, tableName = getString("TABLE_NAME"), tableType = getString("TABLE_TYPE"), remarks = getString("REMARKS")?.trim()
                        ?: "", primaryKeyNames = primaryKeyNames, indexes = indexes, pumlColumns = columns.toMutableList())
            }
        }
        if (table == null) {
            System.err.println("未在${databaseProductName}数据库(${tableNames().joinToString()})中找到${tableName}表")
        }
        return table
    }

    private fun fixColumns(tableName: String, columns: MutableList<Column>) {
        val databaseDriver = DatabaseDriver.fromJdbcUrl(metaData.url)
        if (arrayOf(DatabaseDriver.MYSQL, DatabaseDriver.MARIADB, DatabaseDriver.H2).contains(databaseDriver)) {
            try {
                val prepareStatement = metaData.connection.prepareStatement("SHOW COLUMNS FROM $tableName")
                prepareStatement.executeQuery().use {
                    val find = columns.find { it.columnName == getString(1) }
                    if (find != null) {
                        try {
                            find.extra = getString(6)
                            if (find.extra.contains("AUTO_INCREMENT", true)) {
                                find.autoIncrement = true
                                find.extra = find.extra.replace("AUTO_INCREMENT", "", true).replace("  ", " ", true).trim()
                            }
                        } catch (ignore: Exception) {
                        }
                        val type = getString(2)
                        val (columnSize, decimalDigits) = PumlConverter.parseType(type)
                        find.typeName = type.substringBefore('(')
                        find.columnSize = columnSize
                        find.decimalDigits = decimalDigits
                    }
                }
            } catch (e: Exception) {
                System.err.println("\"SHOW COLUMNS FROM $tableName\"出错:${e.message}")
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
                metaData.getColumns(catalog, curentSchema, curentTableName, null).use {
                    fillColumn(columns)
                }
            } else {
                columnNames.forEach {
                    metaData.getColumns(catalog, curentSchema, curentTableName, it).use {
                        fillColumn(columns)
                    }
                }
            }
        }
        return columns
    }

    private fun fixImportedKeys(curentSchema: String?, curentTableName: String, columns: MutableList<Column>) {
        metaData.getImportedKeys(catalog, curentSchema, curentTableName).use {
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
        if (debug)
            debug("column", rsmd)
        val columnName = getString("COLUMN_NAME")
        val typeName = getString("TYPE_NAME")
        val dataType = getInt("DATA_TYPE")
        val nullable = getInt("NULLABLE") == 1
        val decimalDigits = getInt("DECIMAL_DIGITS")
        val columnDef = getString("COLUMN_DEF")?.trim()?.trim('\'')?.trim()
        val columnSize = getInt("COLUMN_SIZE")
        val remarks = getString("REMARKS")?.replace("[\t\n\r]", "")?.trim()
                ?: ""
        val tableCat = getString("TABLE_CAT")
        val tableSchem = getString("TABLE_SCHEM")
        val column = Column(tableCat = tableCat, tableSchem = tableSchem, columnName = columnName, typeName = typeName, dataType = dataType, decimalDigits = decimalDigits, columnSize = columnSize, remarks = remarks, nullable = nullable, columnDef = columnDef)
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
        println("-----------------------------------------------------")
        for (i in 1..colCount) {
            println("::$name::${metaData.getColumnName(i)}:${getString(i)}")
        }
        println("-----------------------------------------------------")
    }

    /**
     * 获取表主键
     * @param tableName 表名
     * @return 主键字段名
     */
    private fun primaryKeyNames(tableName: String): MutableList<String> {
        val primaryKeys = mutableListOf<String>()
        tableName.current { curentSchema, curentTableName ->
            metaData.getPrimaryKeys(catalog, curentSchema, curentTableName).use {
                primaryKeys.add(getString("COLUMN_NAME"))
            }
        }

        return primaryKeys
    }

    private fun indexes(tableName: String): MutableList<Indexed> {
        val indexes = mutableListOf<Indexed>()
        tableName.current { curentSchema, curentTableName ->
            metaData.getIndexInfo(catalog, curentSchema, curentTableName, false, false).use {
                val indexName = getString("INDEX_NAME")
                if (!indexName.isNullOrBlank() && !"PRIMARY".equals(indexName, true)) {
                    var indexed = indexes.find { it.name == indexName }
                    if (indexed == null) {
                        indexed = Indexed(indexName, !getBoolean("NON_UNIQUE"))
                        indexes.add(indexed)
                    }
                    indexed.columnName.add(getString("COLUMN_NAME"))
                }
            }
        }
        return indexes
    }
}