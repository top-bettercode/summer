package top.bettercode.summer.tools.generator.powerdesigner

import org.dom4j.Element
import org.dom4j.Namespace
import org.dom4j.QName
import org.dom4j.io.SAXReader
import top.bettercode.summer.tools.generator.database.entity.Column
import top.bettercode.summer.tools.generator.database.entity.Indexed
import top.bettercode.summer.tools.generator.database.entity.Table
import top.bettercode.summer.tools.generator.dom.java.JavaTypeResolver
import java.io.File

/**
 *
 * @author Peter Wu
 */
object PdmReader {

    fun read(
        pdmFile: File,
        call: (Table) -> Unit = {}
    ): List<Table> {
        val saxReader = SAXReader()
        val document = saxReader.read(pdmFile)
        val rootElement = document.rootElement


        val oNamespace = Namespace("o", "object")
        val cNamespace = Namespace("c", "collection")
        val aNamespace = Namespace("a", "attribute")

        val rootObject = rootElement.element(QName("RootObject", oNamespace))

        val children = rootObject.element(QName("Children", cNamespace))
        val model = children.element(QName("Model", oNamespace))

        val tables = ArrayList<Table>()

        //解析package
        val packagesEle = model.element(QName("Packages", cNamespace))
        if (packagesEle != null) {
            val packageEles = packagesEle.elements(QName("Package", oNamespace))
            for (packageEle in packageEles) {
                val tablesEle = packageEle.element(QName("Tables", cNamespace))
                if (tablesEle != null) {
                    val tableElement = tablesEle.elements(QName("Table", oNamespace))
                    tableElement.forEach {
                        tables.add(
                            readTable(
                                it,
                                aNamespace,
                                cNamespace,
                                oNamespace,
                                call
                            )
                        )
                    }
                }
            }
        }

        //直接解析table
        val tablesEle = model.element(QName("Tables", cNamespace))
        if (tablesEle != null) {
            val elements = tablesEle.elements(QName("Table", oNamespace))
            elements.forEach {
                tables.add(readTable(it, aNamespace, cNamespace, oNamespace, call))
            }
        }
        return tables.sortedBy { it.tableName }
    }

    private fun readTable(
        tableElement: Element,
        aNamespace: Namespace,
        cNamespace: Namespace,
        oNamespace: Namespace,
        call: (Table) -> Unit = {}
    ): Table {
        val name = tableElement.element(QName("Name", aNamespace))?.textTrim ?: ""
        val code = tableElement.element(QName("Code", aNamespace))?.textTrim
        val physicalOptions = tableElement.element(QName("PhysicalOptions", aNamespace))?.textTrim
            ?: ""
        //解析主键
        val primaryKeyEle = tableElement.element(QName("PrimaryKey", cNamespace))
        val pkRefs = ArrayList<String>()
        if (primaryKeyEle != null) {
            val pks = primaryKeyEle.elements(QName("Key", oNamespace))
            for (pk in pks) {
                pkRefs.add(pk.attribute("Ref").value)
            }
        }
        val columnElements =
            tableElement.element(QName("Columns", cNamespace)).elements(QName("Column", oNamespace))
        val indexes = mutableListOf<Indexed>()
        val keysEle = tableElement.element(QName("Keys", cNamespace))
        val pkIds = ArrayList<String>()
        if (keysEle != null) {
            val keyEleList = keysEle.elements(QName("Key", oNamespace))
            for (keyEle in keyEleList) {
                val id = keyEle.attribute("Id")
                val list = keyEle.element(QName("Key.Columns", cNamespace))
                    .elements(QName("Column", oNamespace))
                if (!pkRefs.contains(id.value)) {
                    Indexed(
                        tableElement.element(QName("Code", aNamespace)).textTrim,
                        false,
                        list.map { c ->
                            columnElements.find {
                                it.attribute("Id").value == c.attribute("Ref").value
                            }!!.element(QName("Code", aNamespace)).textTrim
                        }.toMutableList()
                    )
                } else {
                    for (element in list) {
                        pkIds.add(element.attribute("Ref").value)
                    }
                }
            }
        }
        val primaryKeyNames = mutableListOf<String>()
        val columns = mutableListOf<Column>()
        //解析column

        for (columnEle in columnElements) {
            val columnId = columnEle.attribute("Id").value
            val cname = columnEle.element(QName("Name", aNamespace))?.textTrim
            val ccode = columnEle.element(QName("Code", aNamespace))?.textTrim
            val cDataType = columnEle.element(QName("DataType", aNamespace))?.textTrim ?: ""
            val cLength = columnEle.element(QName("Length", aNamespace))?.textTrim?.toInt() ?: 0
            val cPrecision = columnEle.element(QName("Precision", aNamespace))?.textTrim?.toInt()
                ?: 0
            val cComment = columnEle.element(QName("Comment", aNamespace))?.textTrim
            val cDefaultValue =
                columnEle.element(QName("DefaultValue", aNamespace))?.textTrim?.trim()?.trim('\'')
            val nullable = columnEle.element(QName("Column.Mandatory", aNamespace))?.textTrim
            val identity = columnEle.element(QName("Identity", aNamespace))?.textTrim == "1"

            if (cDataType.isBlank()) {
                println("未识别COLUMN：$code:$ccode")
            }
            val typeName = cDataType.substringBefore("(")
            val column = Column(
                tableCat = null,
                columnName = ccode!!,
                remarks = cComment
                    ?: cname
                    ?: "",
                typeName = typeName,
                dataType = JavaTypeResolver.calculateDataType(typeName),
                columnSize = cLength,
                decimalDigits = cPrecision,
                nullable = nullable?.toBoolean()
                    ?: true,
                unique = false,
                indexed = false,
                columnDef = cDefaultValue,
                extra = "",
                tableSchem = null,
                isForeignKey = false,
                pktableName = "",
                pkcolumnName = "",
                autoIncrement = identity
            )
            if (pkIds.contains(columnId)) {
                column.isPrimary = true
                primaryKeyNames.add(ccode)
            }
            columns.add(column)
        }

        val table = Table(
            productName = top.bettercode.summer.tools.generator.DataType.PUML.name,
            catalog = null,
            schema = null,
            tableName = code!!,
            tableType = "",
            remarks = name,
            primaryKeyNames = primaryKeyNames,
            indexes = indexes,
            pumlColumns = columns,
            physicalOptions = physicalOptions
        )
        call(table)
        return table
    }
}