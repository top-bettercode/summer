package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.*
import org.springframework.util.Assert
import org.springframework.util.StreamUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import top.bettercode.summer.tools.excel.CellStyle.Companion.style
import top.bettercode.summer.web.form.IFormkeyService.Companion.log
import java.io.*
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Consumer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.set

/**
 * 导出Excel文件（导出“XLSX”格式，支持大数据量导出 ）
 */
class ExcelExport {
    private val byteArrayOutputStream: ByteArrayOutputStream?
    private val outputStream: OutputStream?

    /**
     * 工作薄对象
     */
    val workbook: Workbook

    /**
     * 工作表对象
     */
    var sheet: Worksheet? = null
        private set

    /**
     * 当前行号
     */
    private var row = 0

    /**
     * 当前单元格号
     */
    private var column = 0

    /**
     * 是否包含批注
     */
    private var includeComment = false
    private var includeHeader = true
    private var finish = false
    private var includeDataValidation = false

    private var fillColor = "F8F8F7"

    private val cellStyle: CellStyle = CellStyle().apply {
        fontColor(Color.BLACK)
        fontName("Arial")
        horizontalAlignment(Alignment.CENTER.value)
        verticalAlignment(Alignment.CENTER.value)
        borderStyle(BorderStyle.THIN)
        borderColor(Color.BLACK)
    }
    private val headerStyle: CellStyle = CellStyle().apply {
        fillColor("808080")
        fontColor(Color.WHITE)
        fontName("Arial")
        horizontalAlignment(Alignment.CENTER.value)
        verticalAlignment(Alignment.CENTER.value)
        borderStyle(BorderStyle.THIN)
        borderColor(Color.BLACK)
        bold()
    }

    private val columnWidths = ColumnWidths()
    private val poiCells: MutableMap<String, MutableList<ExcelCell<Any>>> = mutableMapOf()

    /**
     * 构造函数
     *
     * @param outputStream          Output stream eventually holding the serialized workbook.
     * @param byteArrayOutputStream byteArrayOutputStream
     */
    private constructor(outputStream: OutputStream, byteArrayOutputStream: ByteArrayOutputStream) {
        this.byteArrayOutputStream = byteArrayOutputStream
        this.outputStream = outputStream
        workbook = Workbook(byteArrayOutputStream, "", "1.0")
    }

    /**
     * 构造函数
     *
     * @param outputStream Output stream eventually holding the serialized workbook.
     */
    private constructor(outputStream: OutputStream) {
        byteArrayOutputStream = null
        this.outputStream = null
        workbook = Workbook(outputStream, "", "1.0")
    }

    fun cellStyle(): CellStyle {
        return this.cellStyle
    }

    fun headerStyle(): CellStyle {
        return this.headerStyle
    }

    fun bHeaderStyle(): ExcelExport {
        this.headerStyle.fillColor("d9d9d9").fontColor(Color.BLACK)
        return this
    }

    /**
     * @param sheetname sheetname
     * @return this
     */
    fun sheet(sheetname: String?): ExcelExport {
        sheet = workbook.newWorksheet(sheetname)
        setRowAndColumn(0, 0)
        columnWidths.clear()
        return this
    }

    /**
     * @return row 行号，从0开始
     */
    fun getRow(): Int {
        return row
    }

    /**
     * @param row 行号，从0开始
     * @return this ExcelExport
     */
    fun setRow(row: Int): ExcelExport {
        this.row = row
        return this
    }

    /**
     * @return column 列号，从0开始
     */
    fun getColumn(): Int {
        return column
    }

    /**
     * @param column 列号，从0开始
     * @return this ExcelExport
     */
    fun setColumn(column: Int): ExcelExport {
        this.column = column
        return this
    }

    /**
     * @param row    行号，从0开始
     * @param column 列号，从0开始
     * @return this ExcelExport
     */
    fun setRowAndColumn(row: Int, column: Int): ExcelExport {
        this.row = row
        this.column = column
        return this
    }

    fun includeDataValidation(): ExcelExport {
        includeDataValidation = true
        return this
    }

    fun includeComment(): ExcelExport {
        includeComment = true
        return this
    }

    fun excludeComment(): ExcelExport {
        includeComment = false
        return this
    }

    fun noHeader(): ExcelExport {
        includeHeader = false
        return this
    }

    @JvmOverloads
    fun createTitle(title: String, cells: Int, headerStyle: CellStyle = this.headerStyle): ExcelExport {
        sheet!!.value(row, column, title)
        val styleSetter = sheet!!.range(row, column, row, column + cells - 1).style()
        styleSetter.style(headerStyle)
        styleSetter.set()
        return this
    }

    fun <T> createHeader(excelFields: Iterable<ExcelField<T, out Any?>>) {
        if (!includeHeader)
            return
        // Create header
        run {
            for (excelField in excelFields) {
                if (byteArrayOutputStream == null && excelField.isPoiColumn) {
                    continue
                }
                val t = excelField.title
                sheet!!.value(row, column, t)
                val width = excelField.width
                if (width == -1.0) {
                    columnWidths.put(column, t)
                    sheet!!.width(column, columnWidths.width(column))
                } else {
                    sheet!!.width(column, width)
                }
                setHeaderStyle()
                val styleSetter = style(sheet!!.range(row + 1, column, row + 1000, column))
                styleSetter.style(excelField.cellStyle)
                if (includeComment) {
                    val commentStr = excelField.comment
                    if (commentStr.isNotBlank()) {
                        sheet!!.comment(row, column, commentStr)
                    }
                }
                if (includeDataValidation && excelField.dataValidation.isNotEmpty()) {
                    dataValidation(column, excelField.dataValidation.joinToString(","))
                }
                column++
            }
        }
        column = 0
        row++
    }


    fun dataValidation(column: Int, dataValidation: Iterable<String>): ExcelExport {
        return dataValidation(column, dataValidation.joinToString(","))
    }

    fun dataValidation(column: Int, dataValidation: String?): ExcelExport {
        Assert.notNull(sheet, "请先初始化sheet")
        val listDataValidation = AbsoluteListDataValidation(
                sheet!!.range(row + 1, column, Worksheet.MAX_ROWS - 1, column), dataValidation)
        listDataValidation.add(sheet!!)
        return this
    }

    @JvmOverloads
    fun <T : Any> setData(list: Iterable<T>, excelFields: Array<ExcelField<T, out Any?>>,
                          converter: (T) -> T = { o: T -> o }): ExcelExport {
        return setData(list, excelFields.asIterable(), converter)
    }

    /**
     * @param <T>         E
     * @param list        list
     * @param excelFields 表格字段
     * @param converter   转换器
     * @return list 数据列表
    </T> */
    @JvmOverloads
    fun <T : Any> setData(list: Iterable<T>, excelFields: Iterable<ExcelField<T, out Any?>>,
                          converter: (T) -> T = { o: T -> o }): ExcelExport {
        Assert.notNull(sheet, "表格未初始化")
        createHeader(excelFields)
        val iterator = list.iterator()
        val firstRow = row
        val firstColumn = column
        while (iterator.hasNext()) {
            val e = converter(iterator.next())
            val lastRow = !iterator.hasNext()
            for (excelField in excelFields) {
                if (byteArrayOutputStream == null && excelField.isPoiColumn) {
                    continue
                }
                setCell(ExcelCell(row, column, firstRow, lastRow, excelField, e))
                column++
            }
            column = firstColumn
            row++
        }
        return this
    }

    @JvmOverloads
    fun <T : Any> setMergeData(list: Iterable<T>, excelFields: Array<ExcelField<T, out Any?>>, converter: (T) -> T = { o: T -> o }): ExcelExport {
        return setMergeData(list, excelFields.asIterable(), converter)
    }

    /**
     * 用于导出有合并行的Execel，第一个ExcelField为mergeId列，此列不导出，用于判断是否合并之前相同mergeId的行
     *
     * @param <T>         E
     * @param list        list
     * @param excelFields 表格字段
     * @param converter   转换器
     * @return list 数据列表
    </T> */
    fun <T : Any> setMergeData(list: Iterable<T>, excelFields: Iterable<ExcelField<T, out Any?>>, converter: (T) -> T): ExcelExport {
        Assert.notNull(sheet, "表格未初始化")
        createHeader(excelFields)
        val iterator = list.iterator()
        val firstRow = row
        val firstColumn = column
        var index = 0
        val firstField: ExcelField<T, *> = if (byteArrayOutputStream == null) {
            excelFields.find { o: ExcelField<T, *> -> !o.isPoiColumn }
                    ?: throw ExcelException("无可导出项目")
        } else {
            excelFields.first()
        }
        val mergeFirstColumn = firstField.isMerge
        val lastMergeIds: MutableMap<Int, Any?> = HashMap()
        val lastRangeTops: MutableMap<Int, Int> = HashMap()
        var preEntity: T? = null
        while (iterator.hasNext()) {
            val e = converter(iterator.next())
            val lastRow = !iterator.hasNext()
            var mergeIndex = 0
            val indexCells: MutableList<ExcelRangeCell<T>>? = if (mergeFirstColumn) null else ArrayList()
            var merge: Boolean
            for (excelField in excelFields) {
                if (byteArrayOutputStream == null && excelField.isPoiColumn) {
                    continue
                }
                merge = excelField.isMerge
                if (merge) {
                    val mergeIdValue = excelField.getMergeId(e)
                    val lastMergeId = lastMergeIds[mergeIndex]
                    var newRange = lastMergeId == null || lastMergeId != mergeIdValue
                    if (newRange) {
                        lastMergeIds[mergeIndex] = mergeIdValue
                    }
                    if (mergeIndex == 0 && newRange) {
                        index++
                    }
                    if (lastRangeTops.getOrDefault(0, firstRow) == row) { //以第一个合并列为大分隔
                        newRange = true
                    }
                    val lastRangeTop = lastRangeTops.getOrDefault(mergeIndex, firstRow)
                    if (newRange) {
                        lastRangeTops[mergeIndex] = row
                    }
                    val rangeCell = ExcelRangeCell(row, column, index, firstRow, lastRow,
                            newRange, lastRangeTop, excelField, preEntity, e)
                    setRangeCell(rangeCell)
                    mergeIndex++
                } else {
                    val rangeCell = ExcelRangeCell(row, column, index, firstRow, lastRow,
                            false, row, excelField, preEntity, e)
                    if (!mergeFirstColumn && excelField.isIndexColumn) {
                        indexCells!!.add(rangeCell)
                    } else {
                        setRangeCell(rangeCell)
                    }
                }
                column++
            }
            if (!mergeFirstColumn) {
                for (indexCell in indexCells!!) {
                    indexCell.setFillColor(index)
                    setRangeCell(indexCell)
                }
            }
            column = firstColumn
            preEntity = e
            row++
        }
        return this
    }

    private fun <T : Any> setCell(excelCell: ExcelCell<T>) {
        val column = excelCell.column
        val row = excelCell.row
        val styleSetter = style(row, column)
        val excelField = excelCell.excelField
        styleSetter.style(excelField.cellStyle)
        if (excelCell.isFillColor) {
            styleSetter.fillColor(fillColor)
        }
        if (excelField.height != -1.0) {
            sheet!!.rowHeight(row, excelField.height)
        }
        styleSetter.set()
        if (excelField.isPoiColumn) {
            @Suppress("UNCHECKED_CAST")
            poiCells.computeIfAbsent(sheet!!.name) { mutableListOf() }.add(excelCell as ExcelCell<Any>)
        }
        if (excelCell.needSetValue()) {
            val cellValue = excelCell.cellValue
            if (cellValue == null || excelField.isPoiColumn) {
                sheet!!.value(row, column)
            } else if (excelField.isFormula) {
                sheet!!.formula(row, column, cellValue as String?)
            } else if (cellValue is String) {
                sheet!!.value(row, column, cellValue)
            } else if (cellValue is Number) {
                sheet!!.value(row, column, cellValue)
            } else if (cellValue is Boolean) {
                sheet!!.value(row, column, cellValue)
            } else if (cellValue is Date) {
                sheet!!.value(row, column, cellValue)
            } else if (cellValue is LocalDateTime) {
                sheet!!.value(row, column, cellValue)
            } else if (cellValue is LocalDate) {
                sheet!!.value(row, column, cellValue)
            } else if (cellValue is ZonedDateTime) {
                sheet!!.value(row, column, cellValue)
            } else {
                throw IllegalArgumentException("No supported cell type for " + cellValue.javaClass)
            }
        } else {
            sheet!!.value(excelCell.row, column)
        }
        val width = excelField.width
        if (width == -1.0) {
            val cellValue = excelCell.cellValue
            columnWidths.put(column, if (excelField.isDateField) excelField.cellStyle.valueFormatting else cellValue)
            if (excelCell.isLastRow) {
                sheet!!.width(column, columnWidths.width(column))
            }
        } else {
            sheet!!.width(column, width)
        }
    }

    private fun <T : Any> setRangeCell(excelCell: ExcelRangeCell<T>) {
        val column = excelCell.column
        setCell(excelCell)
        if (excelCell.needRange) {
            sheet!!.range(excelCell.lastRangeTop, column, excelCell.lastRangeBottom, column)
                    .merge()
            val excelField = excelCell.excelField
            val width = excelField.width
            if (width == -1.0) {
                sheet!!.width(column, columnWidths.width(column))
            } else {
                sheet!!.width(column, width)
            }
            val styleSetter = style(sheet!!.range(excelCell.lastRangeTop, column, excelCell.lastRangeBottom, column))
            styleSetter.style(excelField.cellStyle)

            styleSetter.set()
        }
    }

    private fun setHeaderStyle() {
        val styleSetter = sheet!!.range(row, column, row, column).style()
        styleSetter.style(headerStyle)
        styleSetter.set()
    }

    fun style(row: Int, column: Int): StyleSetter {
        return style(sheet!!.range(row, column, row, column))
    }

    fun style(range: Range): StyleSetter {
        val styleSetter = range.style()
        styleSetter.style(cellStyle)
        return styleSetter
    }

    fun <T : Any> template(excelFields: Array<ExcelField<T, *>>): ExcelExport {
        return template(excelFields.asIterable())
    }

    fun <T : Any> template(excelFields: Iterable<ExcelField<T, *>>): ExcelExport {
        includeComment = true
        setData(emptyList(), excelFields)
        return this
    }

    /**
     * 输出数据流
     */
    fun finish(): ExcelExport {
        if (!finish) {
            finish = try {
                workbook.finish()
                true
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        if (byteArrayOutputStream != null) {
            Assert.notNull(outputStream, "输出流未设置")
            if (poiCells.isEmpty()) {
                outputStream!!.write(byteArrayOutputStream.toByteArray())
            } else {
                PoiExcelUtil.setPoi(poiCells,
                        ByteArrayInputStream(byteArrayOutputStream.toByteArray()), outputStream!!)
                poiCells.clear()
            }
        }
        return this
    }

    companion object {
        /**
         * @param filename filename eventually holding the serialized workbook .
         * @return ExcelExport
         * @throws FileNotFoundException FileNotFoundException
         */
        @JvmStatic
        fun of(filename: String): ExcelExport {
            return ExcelExport(Files.newOutputStream(Paths.get(filename)))
        }

        /**
         * @param file filename eventually holding the serialized workbook .
         * @return ExcelExport
         * @throws FileNotFoundException FileNotFoundException
         */
        @JvmStatic
        fun of(file: File): ExcelExport {
            val parentFile = file.parentFile
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            return ExcelExport(Files.newOutputStream(file.toPath()))
        }

        /**
         * @param outputStream Output stream eventually holding the serialized workbook.
         * @return ExcelExport
         */
        @JvmStatic
        fun of(outputStream: OutputStream): ExcelExport {
            return ExcelExport(outputStream)
        }

        @JvmStatic
        fun withPoi(outputStream: OutputStream): ExcelExport {
            return ExcelExport(outputStream, ByteArrayOutputStream())
        }

        /**
         * 输出数据流
         *
         * @param fileName 输出文件名
         * @param consumer 处理生成excel
         * @throws IOException IOException
         */
        @JvmOverloads
        @JvmStatic
        fun export(fileName: String, createExcelExport: (OutputStream) -> ExcelExport = { outputStream -> of(outputStream) }, cacheKey: String? = null, consumer: Consumer<ExcelExport>) {
            val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as ServletRequestAttributes
            Assert.notNull(requestAttributes, "requestAttributes获取失败")
            val request = requestAttributes.request
            val response = requestAttributes.response!!
            excelContentDisposition(request, response, fileName)
            if (cacheKey != null) {
                val tmpPath = System.getProperty("java.io.tmpdir")
                val file = File(tmpPath,
                        """summer${File.separator}excel-export${File.separator}$fileName${File.separator}$cacheKey.xlsx""")
                if (!file.exists()) {
                    val dir = file.parentFile
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val tmpFile = File(file.toString() + "-" + UUID.randomUUID())
                    Files.newOutputStream(tmpFile.toPath()).use { outputStream ->
                        val excelExport = createExcelExport(outputStream)
                        consumer.accept(excelExport)
                        excelExport.finish()
                    }
                    log.info("输出到缓存文件：{}", file.absolutePath)
                    tmpFile.renameTo(file)
                }
                StreamUtils.copy(Files.newInputStream(file.toPath()), response.outputStream)
            } else {
                val excelExport = createExcelExport(response.outputStream)
                consumer.accept(excelExport)
                excelExport.finish()
            }
        }

        /**
         * 输出数据流
         *
         * @param fileName 输出文件名
         * @param consumer 处理生成excel
         * @throws IOException IOException
         */
        @JvmOverloads
        @JvmStatic
        fun sheet(fileName: String, createExcelExport: (OutputStream) -> ExcelExport = { outputStream -> of(outputStream) }, cacheKey: String? = null, consumer: Consumer<ExcelExport>) {
            export(fileName, createExcelExport, cacheKey) {
                it.sheet("sheet1")
                consumer.accept(it)
            }
        }

        private fun excelContentDisposition(request: HttpServletRequest, response: HttpServletResponse, fileName: String) {
            response.reset()
            attachmentContentDisposition(request, response, "$fileName.xlsx")
        }

        /**
         * 输出到客户端
         *
         * @param request  request
         * @param response response
         * @param fileName 输出文件名
         * @throws IOException IOException
         */
        @JvmStatic
        @JvmOverloads
        fun attachmentContentDisposition(request: HttpServletRequest, response: HttpServletResponse, fileName: String, contentType: String = "application/vnd.ms-excel; charset=utf-8") {
            val agent = request.getHeader("USER-AGENT")
            val encodeFileName = URLEncoder.encode(fileName, "UTF-8")
            val newFileName: String = if (null != agent && (agent.contains("Trident") || agent.contains("Edge"))) {
                encodeFileName
            } else {
                fileName
            }
            response.setHeader("Content-Disposition", "attachment;filename=$newFileName;filename*=UTF-8''$encodeFileName")
            response.contentType = contentType
            response.setHeader("Pragma", "No-cache")
            response.setHeader("Cache-Control", "no-cache")
            response.setDateHeader("Expires", 0)
        }
    }
}
