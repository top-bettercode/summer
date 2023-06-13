package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.*
import org.springframework.util.Assert
import org.springframework.util.StreamUtils
import org.springframework.util.StringUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.*
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Consumer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList
import kotlin.collections.Collection
import kotlin.collections.HashMap
import kotlin.collections.Iterable
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.emptyList
import kotlin.collections.set

/**
 * 导出Excel文件（导出“XLSX”格式，支持大数据量导出 ）
 */
class ExcelExport {
    private val imageByteArrayOutputStream: ByteArrayOutputStream?
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
    private var r = 0

    /**
     * 当前单元格号
     */
    private var c = 0

    /**
     * 是否包含批注
     */
    private var includeComment = false
    private var finish = false
    private var includeDataValidation = false
    private val columnWidths = ColumnWidths()
    private val imageCells: MutableList<ExcelCell<*>> = ArrayList()

    /**
     * 构造函数
     *
     * @param outputStream          Output stream eventually holding the serialized workbook.
     * @param byteArrayOutputStream byteArrayOutputStream
     */
    private constructor(outputStream: OutputStream, byteArrayOutputStream: ByteArrayOutputStream) {
        imageByteArrayOutputStream = byteArrayOutputStream
        this.outputStream = outputStream
        workbook = Workbook(byteArrayOutputStream, "", "1.0")
    }

    /**
     * 构造函数
     *
     * @param outputStream Output stream eventually holding the serialized workbook.
     */
    private constructor(outputStream: OutputStream) {
        imageByteArrayOutputStream = null
        this.outputStream = null
        workbook = Workbook(outputStream, "", "1.0")
    }

    /**
     * @param sheetname sheetname
     * @return this
     */
    fun sheet(sheetname: String?): ExcelExport {
        sheet = workbook.newWorksheet(sheetname)
        setRowAndColumn(0, 0)
        return this
    }

    /**
     * @param row 行号，从0开始
     * @return this ExcelExport
     */
    fun setRow(row: Int): ExcelExport {
        r = row
        return this
    }

    /**
     * @param column 列号，从0开始
     * @return this ExcelExport
     */
    fun setColumn(column: Int): ExcelExport {
        c = column
        return this
    }

    /**
     * @param row    行号，从0开始
     * @param column 列号，从0开始
     * @return this ExcelExport
     */
    fun setRowAndColumn(row: Int, column: Int): ExcelExport {
        r = row
        c = column
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

    fun <T> createHeader(excelFields: Array<ExcelField<T, out Any?>>) {
        // Create header
        run {
            for (excelField in excelFields) {
                if (imageByteArrayOutputStream == null && excelField.isImageColumn) {
                    continue
                }
                val t = excelField.title()
                sheet!!.value(r, c, t)
                val width = excelField.width()
                if (width == -1.0) {
                    columnWidths.put(c, t)
                    sheet!!.width(c, columnWidths.width(c))
                } else {
                    sheet!!.width(c, width)
                }
                setHeaderStyle()
                sheet!!.range(r + 1, c, r + 1000, c).style().format(excelField.format())
                if (includeComment) {
                    val commentStr = excelField.comment()
                    if (StringUtils.hasText(commentStr)) {
                        sheet!!.comment(r, c, commentStr)
                    }
                }
                if (includeDataValidation && excelField.dataValidation().isNotEmpty()) {
                    val listDataValidation = AbsoluteListDataValidation(
                            sheet!!.range(r + 1, c, Worksheet.MAX_ROWS - 1, c), excelField.dataValidation().joinToString(","))
                    listDataValidation.add(sheet!!)
                }
                c++
            }
        }
        c = 0
        r++
    }

    private fun setHeaderStyle() {
        sheet!!.style(r, c)
                .horizontalAlignment(Alignment.CENTER.value)
                .verticalAlignment(Alignment.CENTER.value)
                .bold()
                .fillColor("808080")
                .fontColor("FFFFFF")
                .borderStyle("thin").borderColor("000000")
                .set()
    }

    fun dataValidation(column: Int, dataValidation: Collection<String?>?): ExcelExport {
        return dataValidation(column, StringUtils.collectionToCommaDelimitedString(dataValidation))
    }

    fun dataValidation(column: Int, dataValidation: String?): ExcelExport {
        Assert.notNull(sheet, "请先初始化sheet")
        val listDataValidation = AbsoluteListDataValidation(
                sheet!!.range(r + 1, column, Worksheet.MAX_ROWS - 1, column), dataValidation)
        listDataValidation.add(sheet!!)
        return this
    }

    /**
     * @param <T>         E
     * @param list        list
     * @param excelFields 表格字段
     * @return list 数据列表
    </T> */
    fun <T> setData(list: Iterable<T>, excelFields: Array<ExcelField<T, *>>): ExcelExport {
        return setData(list, excelFields) { o: T -> o }
    }

    /**
     * @param <T>         E
     * @param list        list
     * @param excelFields 表格字段
     * @param converter   转换器
     * @return list 数据列表
    </T> */
    fun <T> setData(list: Iterable<T>, excelFields: Array<ExcelField<T, out Any?>>,
                    converter: (T) -> T): ExcelExport {
        Assert.notNull(sheet, "表格未初始化")
        createHeader(excelFields)
        val iterator = list.iterator()
        val firstRow = r
        val firstColumn = c
        while (iterator.hasNext()) {
            val e = converter(iterator.next())
            val lastRow = !iterator.hasNext()
            for (excelField in excelFields) {
                if (imageByteArrayOutputStream == null && excelField.isImageColumn) {
                    continue
                }
                setCell(ExcelCell(r, c, firstRow, lastRow, excelField, e))
                c++
            }
            c = firstColumn
            r++
        }
        return this
    }

    private fun <T> setCell(excelCell: ExcelCell<T>) {
        val column = excelCell.column
        val row = excelCell.row
        val style = sheet!!.style(row, column)
        val excelField = excelCell.excelField
        val format = excelField.format()
        style.horizontalAlignment(excelField.align().value)
                .verticalAlignment(Alignment.CENTER.value)
                .wrapText(excelField.wrapText())
                .format(format)
                .borderStyle("thin")
                .borderColor("000000")
        if (excelCell.isFillColor) {
            style.fillColor("F8F8F7")
        }
        if (excelField.height() != -1.0) {
            sheet!!.rowHeight(row, excelField.height())
        }
        style.set()
        if (excelField.isImageColumn) {
            imageCells.add(excelCell)
        }
        if (excelCell.needSetValue()) {
            val cellValue = excelCell.cellValue
            if (cellValue == null) {
                sheet!!.value(row, column)
            } else if (excelField.isImageColumn) {
                sheet!!.value(excelCell.row, column)
            } else if (cellValue is String) {
                sheet!!.value(row, column, cellValue)
            } else if (cellValue is Number) {
                sheet!!.value(row, column, cellValue)
            } else if (cellValue is Boolean) {
                sheet!!.value(row, column, cellValue)
            } else if (cellValue is Date) {
                sheet!!.value(row, column, TimestampUtil.convertDate(cellValue))
            } else if (cellValue is LocalDateTime) {
                sheet!!.value(row, column, TimestampUtil.convertDate(
                        Date.from(cellValue.atZone(ZoneId.systemDefault()).toInstant())))
            } else if (cellValue is LocalDate) {
                sheet!!.value(row, column, TimestampUtil.convertDate(cellValue))
            } else if (cellValue is ZonedDateTime) {
                sheet!!.value(row, column, TimestampUtil.convertZonedDateTime(cellValue))
            } else {
                throw IllegalArgumentException("No supported cell type for " + cellValue.javaClass)
            }
        } else {
            sheet!!.value(excelCell.row, column)
        }
        val width = excelField.width()
        if (width == -1.0) {
            val cellValue = excelCell.cellValue
            columnWidths.put(column, if (excelField.isDateField) format else cellValue)
            if (excelCell.isLastRow) {
                sheet!!.width(column, columnWidths.width(column))
            }
        } else {
            sheet!!.width(column, width)
        }
    }

    /**
     * @param <T>         E
     * @param list        list
     * @param excelFields 表格字段
     * @return list 数据列表
    </T> */
    fun <T> setMergeData(list: Iterable<T>, excelFields: Array<ExcelField<T, out Any?>>): ExcelExport {
        return setMergeData(list, excelFields) { o: T -> o }
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
    fun <T> setMergeData(list: Iterable<T>, excelFields: Array<ExcelField<T, out Any?>>,
                         converter: (T) -> T): ExcelExport {
        Assert.notNull(sheet, "表格未初始化")
        createHeader(excelFields)
        val iterator = list.iterator()
        val firstRow = r
        val firstColumn = c
        var index = 0
        val firstField: ExcelField<T, *> = if (imageByteArrayOutputStream == null) {
            Arrays.stream(excelFields).filter { o: ExcelField<T, *> -> !o.isImageColumn }.findFirst()
                    .orElseThrow { ExcelException("无可导出项目") }
        } else {
            excelFields[0]
        }
        val mergeFirstColumn = firstField.isMerge
        val lastMergeIds: MutableMap<Int, Any?> = HashMap()
        val lastRangeTops: MutableMap<Int, Int> = HashMap()
        var preEntity: T? = null
        while (iterator.hasNext()) {
            val e = converter(iterator.next())
            val lastRow = !iterator.hasNext()
            var mergeIndex = 0
            val indexCells: MutableList<ExcelRangeCell<T>>? = if (mergeFirstColumn) null else ArrayList<ExcelRangeCell<T>>()
            var merge: Boolean
            for (excelField in excelFields) {
                if (imageByteArrayOutputStream == null && excelField.isImageColumn) {
                    continue
                }
                merge = excelField.isMerge
                if (merge) {
                    val mergeIdValue = excelField.mergeId(e)
                    val lastMergeId = lastMergeIds[mergeIndex]
                    var newRange = lastMergeId == null || lastMergeId != mergeIdValue
                    if (newRange) {
                        lastMergeIds[mergeIndex] = mergeIdValue
                    }
                    if (mergeIndex == 0 && newRange) {
                        index++
                    }
                    if (lastRangeTops.getOrDefault(0, firstRow) == r) { //以第一个合并列为大分隔
                        newRange = true
                    }
                    val lastRangeTop = lastRangeTops.getOrDefault(mergeIndex, firstRow)
                    if (newRange) {
                        lastRangeTops[mergeIndex] = r
                    }
                    val rangeCell = ExcelRangeCell(r, c, index, firstRow, lastRow,
                            newRange, lastRangeTop, excelField, preEntity, e)
                    setRangeCell(rangeCell)
                    mergeIndex++
                } else {
                    val rangeCell = ExcelRangeCell(r, c, index, firstRow, lastRow,
                            false, r, excelField, preEntity, e)
                    if (!mergeFirstColumn && excelField.isIndexColumn) {
                        indexCells!!.add(rangeCell)
                    } else {
                        setRangeCell(rangeCell)
                    }
                }
                c++
            }
            if (!mergeFirstColumn) {
                for (indexCell in indexCells!!) {
                    indexCell.setFillColor(index)
                    setRangeCell(indexCell)
                }
            }
            c = firstColumn
            preEntity = e
            r++
        }
        return this
    }

    private fun <T> setRangeCell(excelCell: ExcelRangeCell<T>) {
        val column = excelCell.column
        setCell(excelCell)
        if (excelCell.needRange()) {
            sheet!!.range(excelCell.lastRangeTop, column, excelCell.lastRangeBottom, column)
                    .merge()
            val excelField = excelCell.excelField
            val width = excelField.width()
            val format = excelField.format()
            if (width == -1.0) {
                sheet!!.width(column, columnWidths.width(column))
            } else {
                sheet!!.width(column, width)
            }
            val style = sheet!!
                    .range(excelCell.lastRangeTop, column, excelCell.lastRangeBottom, column)
                    .style()
            style.horizontalAlignment(excelField.align().value)
                    .verticalAlignment(Alignment.CENTER.value)
                    .wrapText(excelField.wrapText())
                    .format(format)
                    .borderStyle("thin")
                    .borderColor("000000")
            style.set()
        }
    }

    fun <T> template(excelFields: Array<ExcelField<T, *>>): ExcelExport {
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
        return this
    }

    fun setImage() {
        setImage(sheet!!.name)
    }

    /**
     * @param sheetName sheetName
     */
    fun setImage(sheetName: String?) {
        Assert.notNull(imageByteArrayOutputStream, "不是支持图片插入的导出")
        ExcelImageCellWriterUtil.setImage(sheetName, imageCells,
                ByteArrayInputStream(imageByteArrayOutputStream!!.toByteArray()), outputStream)
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
        fun withImage(outputStream: OutputStream): ExcelExport {
            return ExcelExport(outputStream, ByteArrayOutputStream())
        }

        /**
         * 输出数据流
         *
         * @param fileName 输出文件名
         * @param consumer 处理生成excel
         * @throws IOException IOException
         */
            @JvmStatic
        fun export(fileName: String, consumer: Consumer<ExcelExport?>) {
            val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as ServletRequestAttributes
            Assert.notNull(requestAttributes, "requestAttributes获取失败")
            val request = requestAttributes.request
            val response = requestAttributes.response!!
            setResponseHeader(request, response, fileName)
            val excelExport = of(response.outputStream)
            consumer.accept(excelExport)
            excelExport.finish()
        }

        /**
         * 输出数据流
         *
         * @param fileName 输出文件名
         * @param consumer 处理生成excel
         * @throws IOException IOException
         */
            @JvmStatic
        fun exportWithImage(fileName: String, consumer: Consumer<ExcelExport?>) {
            val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as ServletRequestAttributes
            Assert.notNull(requestAttributes, "requestAttributes获取失败")
            val request = requestAttributes.request
            val response = requestAttributes.response!!
            setResponseHeader(request, response, fileName)
            val excelExport = withImage(response.outputStream)
            consumer.accept(excelExport)
            excelExport.finish()
        }

        /**
         * 输出数据流
         *
         * @param fileName 输出文件名
         * @param consumer 处理生成excel
         * @throws IOException IOException
         */
            @JvmStatic
        fun sheet(fileName: String, consumer: Consumer<ExcelExport?>) {
            val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as ServletRequestAttributes
            Assert.notNull(requestAttributes, "requestAttributes获取失败")
            val request = requestAttributes.request
            val response = requestAttributes.response!!
            setResponseHeader(request, response, fileName)
            val excelExport = of(response.outputStream)
            excelExport.sheet("sheet1")
            consumer.accept(excelExport)
            excelExport.finish()
        }

        /**
         * 文件缓存输出
         *
         * @param fileName 输出文件名
         * @param fileKey  文件唯一key
         * @param consumer 处理生成excel
         * @throws IOException IOException
         */
            @JvmStatic
        fun cache(fileName: String, fileKey: String, consumer: Consumer<ExcelExport?>) {
            cacheOutput(fileName, fileKey) { outputStream: OutputStream ->
                val excelExport = of(outputStream)
                consumer.accept(excelExport)
                excelExport.finish()
            }
        }

        /**
         * 文件缓存输出
         *
         * @param fileName 输出文件名
         * @param fileKey  文件唯一key
         * @param consumer 处理生成excel至 outputStream
         * @throws IOException IOException
         */
            @JvmStatic
        fun cacheOutput(fileName: String, fileKey: String, consumer: (OutputStream) -> ExcelExport) {
            val requestAttributes = RequestContextHolder
                    .getRequestAttributes() as ServletRequestAttributes
            Assert.notNull(requestAttributes, "requestAttributes获取失败")
            val request = requestAttributes.request
            val response = requestAttributes.response!!
            setResponseHeader(request, response, fileName)
            val tmpPath = System.getProperty("java.io.tmpdir")
            val file = File(tmpPath,
                    "excel-export" + File.separator + fileName + File.separator + fileKey + ".xlsx")
            if (!file.exists()) {
                val dir = file.parentFile
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val tmpFile = File(file.toString() + "-" + UUID.randomUUID())
                Files.newOutputStream(tmpFile.toPath()).use { outputStream -> consumer(outputStream) }
                tmpFile.renameTo(file)
            }
            StreamUtils.copy(Files.newInputStream(file.toPath()), response.outputStream)
        }

        /**
         * 输出到客户端
         *
         * @param request  request
         * @param response response
         * @param fileName 输出文件名
         * @throws IOException IOException
         */
            private fun setResponseHeader(request: HttpServletRequest, response: HttpServletResponse,
                                      fileName: String) {
            response.reset()
            val agent = request.getHeader("USER-AGENT")
            val newFileName: String = if (null != agent && (agent.contains("Trident") || agent.contains("Edge"))) {
                URLEncoder.encode(fileName, "UTF-8")
            } else {
                fileName
            }
            response.setHeader("Content-Disposition",
                    "attachment;filename=$newFileName.xlsx;filename*=UTF-8''" + URLEncoder
                            .encode(fileName, "UTF-8") + ".xlsx")
            response.contentType = "application/vnd.ms-excel; charset=utf-8"
            response.setHeader("Pragma", "No-cache")
            response.setHeader("Cache-Control", "no-cache")
            response.setDateHeader("Expires", 0)
        }
    }
}
