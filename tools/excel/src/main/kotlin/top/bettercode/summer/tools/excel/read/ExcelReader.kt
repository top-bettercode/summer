package top.bettercode.summer.tools.excel.read

import org.dhatim.fastexcel.reader.ExcelReaderException
import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import org.dhatim.fastexcel.reader.Sheet
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import top.bettercode.summer.tools.excel.ExcelException
import top.bettercode.summer.tools.excel.read.CellGetter.Companion.isNullOrBlank
import top.bettercode.summer.tools.excel.read.CellGetter.Companion.value
import top.bettercode.summer.tools.excel.read.ExcelReaderException.CellError
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.validation.Validation
import javax.validation.groups.Default
import kotlin.streams.asSequence

/**
 * 导入Excel文件
 */
class ExcelReader private constructor(`is`: InputStream) : Closeable {
    /**
     * 工作表对象
     */
    private var workbook: ReadableWorkbook = try {
        ReadableWorkbook(`is`)
    } catch (e: IOException) {
        throw ExcelException("Excel读取失败，仅支持.xlsx格式Excel文件", e)
    } catch (e: ExcelReaderException) {
        throw ExcelException("Excel读取失败，仅支持.xlsx格式Excel文件", e)
    }

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
    var sheet: Sheet?
        private set

    init {
        try {
            sheet = workbook.firstSheet
        } catch (e: Exception) {
            throw ExcelException("Excel读取失败，文档中没有工作表", e)
        }
        cell(1, 0)
        log.debug("Initialize success.")
    }

    /**
     * @param validateGroups 验证 groups
     */
    fun validateGroups(vararg validateGroups: Class<*>): ExcelReader {
        this.validateGroups = validateGroups.toList().toTypedArray()
        return this
    }

    /**
     * @param row 行号，从0开始
     */
    fun row(row: Int): ExcelReader {
        this.row = row
        return this
    }

    /**
     * @param column 列号，从0开始
     */
    fun column(column: Int): ExcelReader {
        this.column = column
        return this
    }

    /**
     * @param row    行号，从0开始
     * @param column 列号，从0开始
     */
    fun cell(row: Int, column: Int): ExcelReader {
        this.row = row
        this.column = column
        return this
    }

    fun sheet(sheetIndex: Int): ExcelReader {
        sheet = workbook.getSheet(sheetIndex)
            .orElseThrow { ExcelException("未找到第" + (sheetIndex + 1) + "张表") }
        cell(1, 0)
        return this
    }

    fun sheet(sheetName: String): ExcelReader {
        sheet = workbook.findSheet(sheetName)
            .orElseThrow { ExcelException("未找到表：$sheetName") }
        cell(1, 0)
        return this
    }

    fun getRows(): List<Row> {
        return if (sheet == null) {
            throw RuntimeException("文档中未找到相应工作表!")
        } else {
            sheet!!.openStream().use {
                it.asSequence()
                    .filter { row: Row? -> row != null && row.rowNum > this.row }
                    .toList()
            }
        }
    }

    @JvmOverloads
    fun <F, E> getData(
        rowGetter: RowGetter<F>,
        cls: Class<F> = rowGetter.entityType,
        converter: ((F) -> E)? = null
    ): List<E> {
        return if (sheet == null) {
            throw RuntimeException("文档中未找到相应工作表!")
        } else {
            sheet!!.openStream().use {
                it.asSequence()
                    .filter { row: Row? -> row != null && row.rowNum > this.row }
                    .mapNotNull { row ->
                        row.read(cls = cls, rowGetter = rowGetter, converter = converter)
                    }.toList()
            }
        }
    }

    @JvmOverloads
    fun <F, E> Row.read(
        rowGetter: RowGetter<F>,
        cls: Class<F> = rowGetter.entityType,
        converter: ((F) -> E)? = null
    ): E? {
        var notAllBlank = false

        val entity = cls.getDeclaredConstructor().newInstance()

        val rowErrors: MutableList<CellError> = ArrayList()

        row = this.rowNum

        val validators = mutableMapOf<CellGetter<F, *>, Pair<Int, Any?>>()
        rowGetter.forEachIndexed { index, excelCell ->
            excelCell.run {
                val column = column + index
                val cellValue = this@read.value(column, excelCell.isDate)
                notAllBlank = notAllBlank || !cellValue.isNullOrBlank()
                try {
                    entity.setProperty(cellValue, ExcelReader.validator, validateGroups)
                } catch (e: Exception) {
                    rowErrors.add(
                        CellError(
                            row = row,
                            column = column,
                            cellGetter = excelCell,
                            value = cellValue,
                            exception = e
                        )
                    )
                }
                val validator = excelCell.validator
                if (validator != null) {
                    validators[excelCell] = column to cellValue
                }
            }
        }
        //validator
        validators.forEach { (excelField, pair) ->
            val validator = excelField.validator
            try {
                validator?.accept(entity)
            } catch (e: Exception) {
                rowErrors.add(
                    CellError(
                        row = row,
                        column = pair.first,
                        cellGetter = excelField,
                        value = pair.second,
                        exception = e
                    )
                )
            }
        }
        return if (notAllBlank) {
            if (rowErrors.isNotEmpty()) {
                val exception = rowErrors[0].exception
                throw ExcelReaderException(exception.message, rowErrors, exception)
            }
            @Suppress("UNCHECKED_CAST")
            converter?.invoke(entity) ?: (entity as E)
        } else {
            null
        }
    }

    override fun close() {
        workbook.close()
    }

    companion object {

        private val log = LoggerFactory.getLogger(ExcelReader::class.java)

        private val validator = Validation.buildDefaultValidatorFactory().validator

        /**
         * @param fileName 导入文件
         */
        @JvmStatic
        fun of(fileName: String): ExcelReader {
            return of(Files.newInputStream(Paths.get(fileName)))
        }

        /**
         * @param file 导入文件对象
         */
        @JvmStatic
        fun of(file: File): ExcelReader {
            return of(Files.newInputStream(file.toPath()))
        }

        /**
         * @param multipartFile 导入文件对象
         */
        @JvmStatic
        fun of(multipartFile: MultipartFile): ExcelReader {
            return of(multipartFile.inputStream)
        }

        /**
         * @param is 输入流
         */
        @JvmStatic
        fun of(`is`: InputStream): ExcelReader {
            return ExcelReader(`is`)
        }

    }
}
