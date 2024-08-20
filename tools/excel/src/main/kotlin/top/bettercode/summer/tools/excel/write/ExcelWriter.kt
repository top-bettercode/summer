package top.bettercode.summer.tools.excel.write

import org.dhatim.fastexcel.Color
import org.slf4j.LoggerFactory
import org.springframework.util.Assert
import org.springframework.util.StreamUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import top.bettercode.summer.tools.excel.*
import top.bettercode.summer.tools.excel.write.style.CellStyle
import top.bettercode.summer.tools.excel.write.style.ColumnWidths
import top.bettercode.summer.tools.lang.util.TimeUtil
import java.io.Closeable
import java.io.File
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
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
class ExcelWriter(val excel: Excel) : Closeable {

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
    private var includeDataValidation = false

    private var finish = false

    private var fillColor = "F8F8F7"

    private val cellStyle: CellStyle = Excel.defaultStyle

    private val headerStyle: CellStyle = Excel.defaultStyle.headerStyle().apply {
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
    fun cell(
        row: Int = this.row,
        column: Int = this.column,
        style: CellStyle = this.cellStyle.clone()
    ): Cell {
        return this.excel.cell(row, column, style)
    }

    fun headerCell(
        row: Int = this.row,
        column: Int = this.column
    ): Cell {
        return this.excel.cell(row, column, this.headerStyle.clone())
    }


    @JvmOverloads
    fun range(
        top: Int,
        left: Int,
        bottom: Int = top,
        right: Int = left,
        style: CellStyle = this.cellStyle.clone()
    ): Range {
        return this.excel.range(top, left, bottom, right, style)
    }

    fun headerRange(
        top: Int,
        left: Int,
        bottom: Int = top,
        right: Int = left,
    ): Range {
        return this.excel.range(top, left, bottom, right, this.headerStyle.clone())
    }

    fun blankHeaderStyle(): ExcelWriter {
        this.headerStyle.fillColor(CellStyle.LIGHT_GRAY).fontColor(Color.BLACK)
        return this
    }

    /**
     * @param sheetname sheetname
     * @return this
     */
    fun sheet(sheetname: String): ExcelWriter {
        excel.sheet(sheetname)
        cell(0, 0)
        columnWidths.clear()
        return this
    }

    /**
     * @return row 行号，从0开始
     */
    fun row(): Int {
        return row
    }

    /**
     * @param row 行号，从0开始
     */
    fun row(row: Int): ExcelWriter {
        this.row = row
        return this
    }

    /**
     * @return column 列号，从0开始
     */
    fun column(): Int {
        return column
    }

    /**
     * @param column 列号，从0开始
     */
    fun column(column: Int): ExcelWriter {
        this.column = column
        return this
    }

    /**
     * @param row    行号，从0开始
     * @param column 列号，从0开始
     */
    fun setCell(row: Int, column: Int): ExcelWriter {
        this.row = row
        this.column = column
        return this
    }

    fun includeDataValidation(): ExcelWriter {
        includeDataValidation = true
        return this
    }

    fun includeComment(): ExcelWriter {
        includeComment = true
        return this
    }

    fun excludeComment(): ExcelWriter {
        includeComment = false
        return this
    }

    fun noHeader(): ExcelWriter {
        includeHeader = false
        return this
    }

    @JvmOverloads
    fun createTitle(
        title: String,
        cells: Int,
        headerStyle: CellStyle = this.headerStyle
    ): ExcelWriter {
        this.excel.value(row, column, title)
        excel.setStyle(row, column, row, column + cells - 1, headerStyle)
        return this
    }

    fun <E> createHeader(
        rowSetter: RowSetter<E>,
        template: Boolean = false
    ) {
        if (!includeHeader)
            return
        // Create header
        rowSetter.forEach { cellSetter ->
            if (this.excel !is PoiExcel && cellSetter.needPOI) {
                return@forEach
            }
            val t = cellSetter.title
            excel.value(row, column, t)
            val width = cellSetter.width
            if (width == -1.0) {
                columnWidths.put(column, t)
                excel.width(column, columnWidths.width(column))
            } else {
                excel.width(column, width)
            }
            excel.setStyle(row, column, row, column, headerStyle)

            if (template) {
                val style = this.cellStyle.clone()
                style.style(cellSetter.style)
                excel.setStyle(row + 1, column, row + 100, column, style)
            }
            if (includeComment) {
                val commentStr = cellSetter.comment
                if (commentStr.isNotBlank()) {
                    excel.comment(row, column, commentStr)
                }
            }
            if (includeDataValidation && cellSetter.dataValidation.isNotEmpty()) {
                this.excel.dataValidation(row, column, *cellSetter.dataValidation)
            }
            column++
        }
        column = 0
        row++
    }


    fun dataValidation(column: Int, vararg dataValidation: String): ExcelWriter {
        this.excel.dataValidation(row, column, *dataValidation)
        return this
    }

    @JvmOverloads
    fun <E> setData(
        list: Iterable<E>,
        rowSetter: RowSetter<E>,
        converter: ((E) -> E)? = null
    ): ExcelWriter {
        return if (rowSetter.needMerge) {
            setRangeData(list, rowSetter, converter)
        } else
            return setRowData(list, rowSetter, converter)
    }

    @JvmOverloads
    fun <E> setRowData(
        list: Iterable<E>,
        rowSetter: RowSetter<E>,
        converter: ((E) -> E)? = null
    ): ExcelWriter {
        val iterator = list.iterator()
        createHeader(rowSetter, !iterator.hasNext())
        val firstRow = row
        val firstColumn = column
        while (iterator.hasNext()) {
            val next = iterator.next()
            val entity = converter?.invoke(next) ?: next
            val isLastRow = !iterator.hasNext()
            rowSetter.forEach { cellSetter ->
                if (this.excel !is PoiExcel && cellSetter.needPOI) {
                    return@forEach
                }
                cellSetter.set(
                    CellData(
                        row = row,
                        column = column,
                        value = cellSetter.toCell(entity),
                        entity = entity,
                        firstRow = firstRow,
                        firstColumn = firstColumn,
                        isLastRow = isLastRow,
                        isIndex = cellSetter is IndexSetter<E, *>,
                    )
                )
                column++
            }
            column = firstColumn
            row++
        }
        return this
    }

    private fun <E> setRangeData(
        list: Iterable<E>,
        rowSetter: RowSetter<E>,
        converter: ((E) -> E)? = null
    ): ExcelWriter {
        createHeader(rowSetter)
        val iterator = list.iterator()
        val firstRow = row
        val firstColumn = column
        var index = 0
        val firstField: CellSetter<E, *> = if (this.excel !is PoiExcel) {
            rowSetter.find { o: CellSetter<E, *> -> !(o is PropertyCellSetter<E, *> && o.setter != null) }
                ?: throw ExcelException("无可导出项目")
        } else {
            rowSetter.first()
        }
        val firstColumnNeedMerge = firstField.needMerge
        val lastMergeIds: MutableMap<Int, Any?> = mutableMapOf()
        val lastRangeTops: MutableMap<Int, Int> = mutableMapOf()
        val lastValues: MutableMap<Int, Any?> = mutableMapOf()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val entity = converter?.invoke(next) ?: next
            val isLastRow = !iterator.hasNext()
            var mergeIndex = 0
            val indexSetters: MutableMap<RangeData<E>, CellSetter<E, *>>? =
                if (firstColumnNeedMerge) null else mutableMapOf()
            var needMerge: Boolean
            rowSetter.forEach { cellSetter ->
                if (this.excel !is PoiExcel && cellSetter.needPOI) {
                    return@forEach
                }
                val value = cellSetter.toCell(entity)
                val preValue = lastValues[column]
                needMerge = cellSetter.needMerge
                if (needMerge) {
                    val mergeIdValue = cellSetter.mergeId(entity)
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
                    cellSetter.set(
                        RangeData(
                            row = row,
                            column = column,
                            value = value,
                            preValue = preValue,
                            entity = entity,
                            index = index,
                            firstRow = firstRow,
                            firstColumn = firstColumn,
                            isLastRow = isLastRow,
                            isIndex = cellSetter is IndexSetter<E, *>,
                            needMerge = true,
                            newRange = newRange,
                            lastRangeTop = lastRangeTop,
                        )
                    )
                    mergeIndex++
                } else {
                    val rangeCell = RangeData(
                        row = row,
                        column = column,
                        value = value,
                        preValue = preValue,
                        entity = entity,
                        index = index,
                        firstRow = firstRow,
                        firstColumn = firstColumn,
                        isLastRow = isLastRow,
                        isIndex = cellSetter is IndexSetter<E, *>,
                        needMerge = false,
                        newRange = false,
                        lastRangeTop = row,
                    )
                    if (!firstColumnNeedMerge && cellSetter is IndexSetter<E, *>) {
                        indexSetters!![rangeCell] = cellSetter
                    } else {
                        cellSetter.set(rangeCell)
                    }
                }
                lastValues[column] = value
                column++
            }
            if (!firstColumnNeedMerge) {
                for ((indexCell, cellSetter) in indexSetters!!) {
                    indexCell.setFillColor(index)
                    cellSetter.set(indexCell)
                }
            }
            column = firstColumn
            row++
        }
        return this
    }

    private fun <E> CellSetter<E, *>.set(cellData: CellData<E>) {
        val row = cellData.row
        val column = cellData.column
        val style = cellStyle.clone()
        if (cellData.isFilledColor) {
            style.fillColor(fillColor)
        }

        val getterStyle = this.style
        this.styleSetter?.accept(getterStyle, cellData.entity)

        style.style(getterStyle)
        excel.setStyle(row, column, row, column, style)

        if (this.height != -1.0 && cellData.isFirstColumn) {
            excel.height(row, this.height)
        }
        val value = cellData.value
        if (value == null) {
            excel.value(row, column)
        } else if (this.needPOI) {
            if (excel is PoiExcel)
                ((this as PropertyCellSetter<E, *>).setter!!)(excel, cellData)
        } else if (this is FormulaSetter<E, *>) {
            excel.formula(row, column, value as String)
        } else if (value is String) {
            excel.value(row, column, value)
        } else if (value is Number) {
            excel.value(row, column, value)
        } else if (value is Boolean) {
            excel.value(row, column, value)
        } else if (value is Date) {
            excel.value(row, column, value)
        } else if (value is LocalDateTime) {
            excel.value(row, column, value)
        } else if (value is LocalDate) {
            excel.value(row, column, value)
        } else if (value is ZonedDateTime) {
            excel.value(row, column, value)
        } else {
            throw IllegalArgumentException("No supported cell type for " + value.javaClass)
        }
        val width = this.width
        if (width == -1.0) {
            columnWidths.put(
                column,
                if (this is PropertyCellSetter<E, *> && this.isDate) getterStyle.valueFormatting else value
            )
            if (cellData.isLastRow) {
                excel.width(column, columnWidths.width(column))
            }
        } else {
            if (cellData.isLastRow)
                excel.width(column, width)
        }
    }

    private fun <E> CellSetter<E, *>.set(rangeData: RangeData<E>) {
        set(rangeData as CellData<E>)
        if (rangeData.needMergeRange) {
            val column = rangeData.column
            excel.merge(rangeData.lastRangeTop, column, rangeData.lastRangeBottom, column)
//            val width = this.width
//            if (width == -1.0) {
//                excel.width(column, columnWidths.width(column))
//            } else {
//                excel.width(column, width)
//            }
        }
    }


    fun <E> template(rowSetter: RowSetter<E>): ExcelWriter {
        includeComment = true
        setData(emptyList(), rowSetter)
        return this
    }

    /**
     * 输出数据流
     */
    override fun close() {
        this.excel.close()
    }

    companion object {

        private val log = LoggerFactory.getLogger(ExcelWriter::class.java)

        @JvmOverloads
        @JvmStatic
        fun of(filename: String, poi: Boolean = false, useSxss: Boolean = true): ExcelWriter {
            val outputStream = Files.newOutputStream(Paths.get(filename))
            return ExcelWriter(
                if (poi)
                    PoiExcel(outputStream, useSxss)
                else
                    FastExcel(outputStream)
            )
        }

        @JvmOverloads
        @JvmStatic
        fun write(
            fileName: String,
            cacheKey: String? = null,
            expiresIn: Duration? = null,
            poi: Boolean = false,
            useSxss: Boolean = true,
            consumer: Consumer<ExcelWriter>
        ) {
            val requestAttributes = RequestContextHolder
                .getRequestAttributes() as ServletRequestAttributes
            Assert.notNull(requestAttributes, "requestAttributes获取失败")
            val request = requestAttributes.request
            val response = requestAttributes.response!!
            excelDisposition(request, response, fileName)
            if (cacheKey != null) {
                val tmpPath = System.getProperty("java.io.tmpdir")
                val file = File(
                    tmpPath,
                    "summer${File.separator}excel-export${File.separator}$cacheKey.xlsx"
                )
                if (expiresIn != null && file.exists()) {
                    val expiresTime =
                        TimeUtil.Companion.toLocalDateTime(file.lastModified()).plus(expiresIn)
                    if (!LocalDateTime.now().isBefore(expiresTime)) {
                        file.delete()
                        log.info("删除过期文件：{}", file.absolutePath)
                    }
                }
                if (!file.exists()) {
                    val dir = file.parentFile
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val tmpFile = File(file.toString() + "-" + UUID.randomUUID())
                    Files.newOutputStream(tmpFile.toPath()).use { outputStream ->
                        ExcelWriter(
                            if (poi)
                                PoiExcel(outputStream, useSxss)
                            else
                                FastExcel(outputStream)
                        ).use {
                            consumer.accept(it)
                        }
                    }
                    log.info("输出到缓存文件：{}", file.absolutePath)
                    tmpFile.renameTo(file)
                } else {
                    log.info("从缓存文件读取：{}", file.absolutePath)
                }
                StreamUtils.copy(Files.newInputStream(file.toPath()), response.outputStream)
            } else {
                val outputStream = response.outputStream
                ExcelWriter(
                    if (poi) PoiExcel(
                        outputStream,
                        useSxss
                    ) else FastExcel(outputStream)
                ).use {
                    consumer.accept(it)
                }
            }
        }

        @JvmOverloads
        @JvmStatic
        fun sheet(
            fileName: String,
            poi: Boolean = false,
            useSxss: Boolean = true,
            cacheKey: String? = null,
            consumer: Consumer<ExcelWriter>
        ) {
            write(fileName = fileName, poi = poi, useSxss = useSxss, cacheKey = cacheKey) {
                it.sheet("sheet1")
                consumer.accept(it)
            }
        }

        private fun excelDisposition(
            request: HttpServletRequest,
            response: HttpServletResponse,
            fileName: String
        ) {
            response.reset()
            attachmentDisposition(request, response, "$fileName.xlsx")
        }

        @JvmStatic
        @JvmOverloads
        fun attachmentDisposition(
            request: HttpServletRequest,
            response: HttpServletResponse,
            fileName: String,
            contentType: String = "application/vnd.ms-excel; charset=utf-8"
        ) {
            val agent = request.getHeader("USER-AGENT")
            val encodeFileName = URLEncoder.encode(fileName, "UTF-8")
            val newFileName: String =
                if (null != agent && (agent.contains("Trident") || agent.contains("Edge"))) {
                    encodeFileName
                } else {
                    fileName
                }
            response.setHeader(
                "Content-Disposition",
                "attachment;filename=$newFileName;filename*=UTF-8''$encodeFileName"
            )
            response.contentType = contentType
            response.setHeader("Pragma", "No-cache")
            response.setHeader("Cache-Control", "no-cache")
            response.setDateHeader("Expires", 0)
        }
    }
}
