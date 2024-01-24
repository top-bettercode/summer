package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.Color
import org.springframework.util.Assert
import org.springframework.util.StreamUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import top.bettercode.summer.web.form.IFormkeyService.Companion.log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
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
 * 导出Excel文件（导出“XLSX”格式）
 */
class ExcelExport(val excel: IExcel) {

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

    private val cellStyle: CellStyle = IExcel.defaultStyle

    private val headerStyle: CellStyle = IExcel.defaultStyle.headerStyle().apply {
        fillColor("808080")
        fontColor(Color.WHITE)
    }

    private val columnWidths = ColumnWidths()

    fun cellStyle(): CellStyle {
        return this.cellStyle
    }

    fun headerStyle(): CellStyle {
        return this.headerStyle
    }

    @JvmOverloads
    fun cell(row: Int = this.row, column: Int = this.column, style: CellStyle = this.cellStyle.clone()): ExcelCell {
        return this.excel.cell(row, column, style)
    }

    @JvmOverloads
    fun range(top: Int, left: Int, bottom: Int = top, right: Int = left, style: CellStyle = this.cellStyle.clone()): ExcelRange {
        return this.excel.range(top, left, bottom, right, style)
    }

    fun blankHeaderStyle(): ExcelExport {
        this.headerStyle.fillColor("d9d9d9").fontColor(Color.BLACK)
        return this
    }

    /**
     * @param sheetname sheetname
     * @return this
     */
    fun sheet(sheetname: String): ExcelExport {
        excel.sheet(sheetname)
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
        this.excel.value(row, column, title)
        excel.setStyle(row, column, row, column + cells - 1, headerStyle)
        return this
    }

    fun <T : Any> createHeader(excelFields: Iterable<ExcelField<T, out Any?>>, template: Boolean = false) {
        if (!includeHeader)
            return
        // Create header
        run {
            for (excelField in excelFields) {
                if (this.excel !is PoiExcel && excelField.cellSetter != null) {
                    continue
                }
                val t = excelField.title
                excel.value(row, column, t)
                val width = excelField.width
                if (width == -1.0) {
                    columnWidths.put(column, t)
                    excel.width(column, columnWidths.width(column))
                } else {
                    excel.width(column, width)
                }
                excel.setStyle(row, column, row, column, headerStyle)

                if (template) {
                    val style = this.cellStyle.clone()
                    style.style(excelField.cellStyle)
                    excel.setStyle(row + 1, column, row + 100, column, style)
                }
                if (includeComment) {
                    val commentStr = excelField.comment
                    if (commentStr.isNotBlank()) {
                        excel.comment(row, column, commentStr)
                    }
                }
                if (includeDataValidation && excelField.dataValidation.iterator().hasNext()) {
                    this.excel.dataValidation(row, column, excelField.dataValidation)
                }
                column++
            }
        }
        column = 0
        row++
    }


    fun dataValidation(column: Int, vararg dataValidation: String): ExcelExport {
        this.excel.dataValidation(row, column, dataValidation)
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
        val iterator = list.iterator()
        createHeader(excelFields, !iterator.hasNext())
        val firstRow = row
        val firstColumn = column
        while (iterator.hasNext()) {
            val e = converter(iterator.next())
            val lastRow = !iterator.hasNext()
            for (excelField in excelFields) {
                if (this.excel !is PoiExcel && excelField.cellSetter != null) {
                    continue
                }
                setCell(ExcelFieldCell(row, column, firstRow, lastRow, excelField, e))
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
        createHeader(excelFields)
        val iterator = list.iterator()
        val firstRow = row
        val firstColumn = column
        var index = 0
        val firstField: ExcelField<T, *> = if (this.excel !is PoiExcel) {
            excelFields.find { o: ExcelField<T, *> -> o.cellSetter == null }
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
            val indexCells: MutableList<ExcelFieldRange<T>>? = if (mergeFirstColumn) null else ArrayList()
            var merge: Boolean
            for (excelField in excelFields) {
                if (this.excel !is PoiExcel && excelField.cellSetter != null) {
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
                    val rangeCell = ExcelFieldRange(row, column, index, firstRow, lastRow,
                            newRange, lastRangeTop, excelField, preEntity, e)
                    setRange(rangeCell)
                    mergeIndex++
                } else {
                    val rangeCell = ExcelFieldRange(row, column, index, firstRow, lastRow,
                            false, row, excelField, preEntity, e)
                    if (!mergeFirstColumn && excelField.isIndexColumn) {
                        indexCells!!.add(rangeCell)
                    } else {
                        setRange(rangeCell)
                    }
                }
                column++
            }
            if (!mergeFirstColumn) {
                for (indexCell in indexCells!!) {
                    indexCell.setFillColor(index)
                    setRange(indexCell)
                }
            }
            column = firstColumn
            preEntity = e
            row++
        }
        return this
    }

    private fun <T : Any> setCell(excelCell: ExcelFieldCell<T>) {
        val column = excelCell.column
        val row = excelCell.row
        val excelField = excelCell.excelField
        val style = this.cellStyle.clone()
        style.style(excelField.cellStyle)
        if (excelCell.isFillColor) {
            style.fillColor(fillColor)
        }
        this.excel.setStyle(row, column, row, column, style)

        if (excelField.height != -1.0) {
            this.excel.height(row, excelField.height)
        }
        if (excelCell.needSetValue()) {
            val cellValue = excelCell.cellValue
            if (cellValue == null) {
                this.excel.value(row, column)
            } else if (excelField.cellSetter != null) {
                if (this.excel is PoiExcel)
                    (excelField.cellSetter!!)(this.excel, excelCell)
            } else if (excelField.isFormula) {
                this.excel.formula(row, column, cellValue as String)
            } else if (cellValue is String) {
                this.excel.value(row, column, cellValue)
            } else if (cellValue is Number) {
                this.excel.value(row, column, cellValue)
            } else if (cellValue is Boolean) {
                this.excel.value(row, column, cellValue)
            } else if (cellValue is Date) {
                this.excel.value(row, column, cellValue)
            } else if (cellValue is LocalDateTime) {
                this.excel.value(row, column, cellValue)
            } else if (cellValue is LocalDate) {
                this.excel.value(row, column, cellValue)
            } else if (cellValue is ZonedDateTime) {
                this.excel.value(row, column, cellValue)
            } else {
                throw IllegalArgumentException("No supported cell type for " + cellValue.javaClass)
            }
        } else {
            this.excel.value(excelCell.row, column)
        }
        val width = excelField.width
        if (width == -1.0) {
            val cellValue = excelCell.cellValue
            columnWidths.put(column, if (excelField.isDateField) excelField.cellStyle.valueFormatting else cellValue)
            if (excelCell.isLastRow) {
                this.excel.width(column, columnWidths.width(column))
            }
        } else {
            this.excel.width(column, width)
        }
    }

    private fun <T : Any> setRange(excelCell: ExcelFieldRange<T>) {
        val column = excelCell.column
        setCell(excelCell)
        if (excelCell.needRange) {
            this.excel.merge(excelCell.lastRangeTop, column, excelCell.lastRangeBottom, column)

            val excelField = excelCell.excelField
            val width = excelField.width
            if (width == -1.0) {
                this.excel.width(column, columnWidths.width(column))
            } else {
                this.excel.width(column, width)
            }
        }
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
                this.excel.finish()
                true
            } catch (e: IOException) {
                throw RuntimeException(e)
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
        @JvmOverloads
        @JvmStatic
        fun of(filename: String, poi: Boolean = false): ExcelExport {
            val outputStream = Files.newOutputStream(Paths.get(filename))
            return ExcelExport(if (poi) PoiExcel(outputStream) else FastExcel(outputStream))
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
        fun export(fileName: String, poi: Boolean = false, cacheKey: String? = null, consumer: Consumer<ExcelExport>) {
            val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as ServletRequestAttributes
            Assert.notNull(requestAttributes, "requestAttributes获取失败")
            val request = requestAttributes.request
            val response = requestAttributes.response!!
            excelDisposition(request, response, fileName)
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
                        val excelExport = ExcelExport(if (poi) PoiExcel(outputStream) else FastExcel(outputStream))
                        consumer.accept(excelExport)
                        excelExport.finish()
                    }
                    log.info("输出到缓存文件：{}", file.absolutePath)
                    tmpFile.renameTo(file)
                } else {
                    log.info("从缓存文件读取：{}", file.absolutePath)
                }
                StreamUtils.copy(Files.newInputStream(file.toPath()), response.outputStream)
            } else {
                val outputStream = response.outputStream
                val excelExport = ExcelExport(if (poi) PoiExcel(outputStream) else FastExcel(outputStream))
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
        fun sheet(fileName: String, poi: Boolean = false, cacheKey: String? = null, consumer: Consumer<ExcelExport>) {
            export(fileName = fileName, poi = poi, cacheKey = cacheKey) {
                it.sheet("sheet1")
                consumer.accept(it)
            }
        }

        private fun excelDisposition(request: HttpServletRequest, response: HttpServletResponse, fileName: String) {
            response.reset()
            attachmentDisposition(request, response, "$fileName.xlsx")
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
        fun attachmentDisposition(request: HttpServletRequest, response: HttpServletResponse, fileName: String, contentType: String = "application/vnd.ms-excel; charset=utf-8") {
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
