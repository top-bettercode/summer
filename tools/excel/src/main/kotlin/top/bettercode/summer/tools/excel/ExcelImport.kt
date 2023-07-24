package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.reader.*
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import top.bettercode.summer.tools.excel.ExcelImport.Companion.validator
import top.bettercode.summer.tools.excel.ExcelImportException.CellError
import top.bettercode.summer.tools.lang.util.TimeUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.validation.Validation
import javax.validation.groups.Default
import kotlin.collections.ArrayList

/**
 * 导入Excel文件
 */
class ExcelImport private constructor(`is`: InputStream) {
    /**
     * 工作表对象
     */
    private var workbook: ReadableWorkbook

    /**
     * 验证 groups
     */
    private var validateGroups = arrayOf<Class<*>>(Default::class.java)

    /**
     * 当前行号
     */
    var row = 1
        private set

    /**
     * 当前单元格号
     */
    var column = 0
        private set

    /**
     * 工作表对象
     */
    private var sheet: Sheet?

    /**
     * 构造函数
     *
     * @param `is` is
     */
    init {
        workbook = try {
            ReadableWorkbook(`is`)
        } catch (e: IOException) {
            throw ExcelException("Excel读取失败，仅支持.xlsx格式Excel文件", e)
        } catch (e: ExcelReaderException) {
            throw ExcelException("Excel读取失败，仅支持.xlsx格式Excel文件", e)
        }
        sheet = workbook.firstSheet
        setRowAndColumn(1, 0)
        log.debug("Initialize success.")
    }

    /**
     * @param row 行号，从0开始
     * @return this ExcelExport
     */
    fun setRow(row: Int): ExcelImport {
        this.row = row
        return this
    }

    /**
     * @param column 列号，从0开始
     * @return this ExcelExport
     */
    fun setColumn(column: Int): ExcelImport {
        this.column = column
        return this
    }

    /**
     * @param row    行号，从0开始
     * @param column 列号，从0开始
     * @return this ExcelExport
     */
    fun setRowAndColumn(row: Int, column: Int): ExcelImport {
        this.row = row
        this.column = column
        return this
    }

    fun sheet(sheetIndex: Int): ExcelImport {
        sheet = workbook.getSheet(sheetIndex)
                .orElseThrow { ExcelException("未找到第" + (sheetIndex + 1) + "张表") }
        setRowAndColumn(1, 0)
        return this
    }

    fun sheet(sheetName: String): ExcelImport {
        sheet = workbook.findSheet(sheetName)
                .orElseThrow { ExcelException("未找到表：$sheetName") }
        setRowAndColumn(1, 0)
        return this
    }

    /**
     * @param validateGroups 验证 groups
     * @return ExcelImport this
     */
    fun validateGroups(vararg validateGroups: Class<*>): ExcelImport {
        this.validateGroups = validateGroups.toList().toTypedArray()
        return this
    }

    /**
     * 获取导入数据列表
     *
     * @param <F>         F
     * @param <E>         E
     * @param excelFields excelFields
     * @return List
     * @throws IOException            IOException
     * @throws IllegalAccessException IllegalAccessException
     * @throws ExcelImportException   ExcelImportException
     * @throws InstantiationException InstantiationException
    </E></F> */
    fun <F, E> getData(excelFields: Array<ExcelField<F, *>>): List<E> {
        return getData<F, E>(getEntityType(excelFields), excelFields)
    }

    /**
     * 获取导入数据列表
     *
     * @param converter   F 转换为E
     * @param <F>         F
     * @param <E>         E
     * @param excelFields excelFields
     * @return List
     * @throws IOException            IOException
     * @throws IllegalAccessException IllegalAccessException
     * @throws ExcelImportException   ExcelImportException
     * @throws InstantiationException InstantiationException
    </E></F> */
    fun <F, E> getData(excelFields: Array<ExcelField<F, *>>, converter: (F) -> E): List<E> {
        return getData<F, E>(getEntityType(excelFields), excelFields, converter)
    }

    /**
     * 获取导入数据列表
     *
     * @param <F>         F
     * @param <E>         E
     * @param excelFields excelFields
     * @param cls         实体类型
     * @return List
     * @throws IOException            IOException
     * @throws IllegalAccessException IllegalAccessException
     * @throws ExcelImportException   ExcelImportException
     * @throws InstantiationException InstantiationException
    </E></F> */
    fun <F, E> getData(cls: Class<F>?, excelFields: Array<ExcelField<F, *>>): List<E> {
        return getData(cls, excelFields) { o: F ->
            @Suppress("UNCHECKED_CAST")
            o as E
        }
    }

    /**
     * 获取导入数据列表
     *
     * @param cls         实体类型
     * @param excelFields excelFields
     * @param converter   F 转换为E
     * @param <F>         F
     * @param <E>         E
     * @return List
     * @throws IOException            IOException
     * @throws IllegalAccessException IllegalAccessException
     * @throws ExcelImportException   ExcelImportException
     * @throws InstantiationException InstantiationException
    </E></F> */
    fun <F, E> getData(cls: Class<F>?, excelFields: Array<ExcelField<F, *>>,
                       converter: (F) -> E): List<E> {
        if (sheet == null) {
            throw RuntimeException("文档中未找到相应工作表!")
        }
        val dataList: MutableList<E> = ArrayList()
        for (row in sheet!!.openStream().filter { row: Row -> row.rowNum > this.row }
                .collect(Collectors.toList())) {
            if (row != null) {
                val e = readRow(cls, excelFields, row, converter)
                if (e != null) {
                    dataList.add(e)
                }
            }
        }
        return dataList
    }

    fun <F, E> readRow(cls: Class<F>?, excelFields: Array<ExcelField<F, *>>, row: Row,
                       converter: (F) -> E): E? {
        var notAllBlank = false
        val o = cls!!.getDeclaredConstructor().newInstance()
        val rowErrors: MutableList<CellError> = ArrayList()
        this.row = row.rowNum
        for ((index, excelField) in excelFields.withIndex()) {
            if (excelField.isIndexColumn) {
                continue
            }
            val column = this.column + index
            val cellValue = getCellValue(excelField, row, column)
            notAllBlank = notAllBlank || !excelField.isEmptyCell(cellValue)
            try {
                excelField.setProperty(o, cellValue, validator, validateGroups)
            } catch (e: Exception) {
                rowErrors.add(CellError(this.row, column, excelField.title, getValue(cellValue), e))
            }
        }
        excelFields.forEachIndexed { index, excelField ->
            val validator = excelField.validator
            try {
                validator?.accept(o)
            } catch (e: Exception) {
                val column = this.column + index
                val cellValue = excelField.toCellValue(o)
                rowErrors.add(CellError(this.row, column, excelField.title, getValue(cellValue), e))
            }
        }
        return if (notAllBlank) {
            if (rowErrors.isNotEmpty()) {
                val exception = rowErrors[0].exception
                throw ExcelImportException(exception.message, rowErrors, exception)
            }
            converter(o)
        } else {
            null
        }
    }

    private fun getValue(cellValue: Any?) = when (cellValue) {
        is Date -> {
            TimeUtil.of(cellValue).format("yyyy-MM-dd HH:mm:ss")
        }

        is LocalDateTime -> {
            TimeUtil.of(cellValue).format("yyyy-MM-dd HH:mm:ss")
        }

        is LocalDate -> {
            TimeUtil.of(cellValue).format("yyyy-MM-dd")
        }

        is ZonedDateTime -> {
            TimeUtil.of(cellValue.toLocalDateTime()).format("yyyy-MM-dd HH:mm:ss")
        }

        else -> {
            cellValue?.toString()
        }
    }

    /**
     * 获取单元格值
     *
     * @param row    获取的行
     * @param column 获取单元格列号
     * @return 单元格值
     */
    private fun getCellValue(excelField: ExcelField<*, *>, row: Row, column: Int): Any? {
        try {
            val cell = row.getCell(column)
            if (cell != null) {
                return when (cell.type!!) {
                    CellType.STRING -> row.getCellAsString(column).orElse(null)
                    CellType.NUMBER -> if (excelField.isDateField) {
                        row.getCellAsDate(column).orElse(null)
                    } else {
                        row.getCellAsNumber(column).orElse(null)
                    }

                    CellType.BOOLEAN -> row.getCellAsBoolean(column).orElse(null)
                    CellType.FORMULA, CellType.EMPTY, CellType.ERROR -> row.getCell(column).value
                }
            }
        } catch (ignored: IndexOutOfBoundsException) {
        }
        return null
    }

    companion object {
        private val log = LoggerFactory.getLogger(ExcelImport::class.java)
        private val validator = Validation.buildDefaultValidatorFactory()
                .validator

        /**
         * @param fileName 导入文件
         * @return ExcelImport
         * @throws IOException IOException
         */
        @JvmStatic
        fun of(fileName: String): ExcelImport {
            return of(Files.newInputStream(Paths.get(fileName)))
        }

        /**
         * @param file 导入文件对象
         * @return ExcelImport
         * @throws IOException IOException
         */
        @JvmStatic
        fun of(file: File): ExcelImport {
            return of(Files.newInputStream(file.toPath()))
        }

        /**
         * @param multipartFile 导入文件对象
         * @return ExcelImport
         * @throws IOException IOException
         */
        @JvmStatic
        fun of(multipartFile: MultipartFile): ExcelImport {
            return of(multipartFile.inputStream)
        }

        /**
         * @param is 输入流
         * @return ExcelImport
         * @throws IOException IOException
         */
        @JvmStatic
        fun of(`is`: InputStream): ExcelImport {
            return ExcelImport(`is`)
        }

        private fun <F> getEntityType(excelFields: Array<ExcelField<F, *>>): Class<F>? {
            for (excelField in excelFields) {
                if (!excelField.isIndexColumn) {
                    return excelField.entityType
                }
            }
            throw ExcelException("只有索引列？")
        }
    }
}
