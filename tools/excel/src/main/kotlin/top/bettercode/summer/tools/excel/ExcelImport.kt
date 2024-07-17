package top.bettercode.summer.tools.excel

import org.dhatim.fastexcel.reader.*
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import top.bettercode.summer.tools.excel.ExcelImportException.CellError
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
class ExcelImport private constructor(`is`: InputStream) {
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

    /**
     * 构造函数
     *
     * @param `is` is
     */
    init {
        try {
            sheet = workbook.firstSheet
        } catch (e: Exception) {
            throw ExcelException("Excel读取失败，文档中没有工作表", e)
        }
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
    @JvmOverloads
    fun <F, E> getData(
        excelFields: Array<ExcelField<F, *>>,
        cls: Class<F> = getEntityType(excelFields.asIterable()),
        converter: ((F) -> E)? = null
    ): List<E> {
        return getData(excelFields = excelFields.asIterable(), cls = cls, converter = converter)
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
    @JvmOverloads
    fun <F, E> getData(
        excelFields: Iterable<ExcelField<F, *>>,
        cls: Class<F> = getEntityType(excelFields),
        converter: ((F) -> E)? = null
    ): List<E> {
        workbook.use {
            if (sheet == null) {
                throw RuntimeException("文档中未找到相应工作表!")
            } else {
                sheet!!.openStream().use {
                    return it.asSequence()
                        .filter { row: Row? -> row != null && row.rowNum > this.row }
                        .mapNotNull { row ->
                            readRow(
                                row = row,
                                cls = cls,
                                excelFields = excelFields,
                                converter = converter
                            )
                        }.toList()
                }
            }
        }
    }

    @JvmOverloads
    fun <F, E> readRow(
        row: Row,
        excelFields: Iterable<ExcelField<F, *>>,
        cls: Class<F> = getEntityType(excelFields),
        converter: ((F) -> E)? = null
    ): E? {
        var notAllBlank = false

        val entity = cls.getDeclaredConstructor().newInstance()

        val rowErrors: MutableList<CellError> = ArrayList()

        this.row = row.rowNum

        val validators = mutableMapOf<ExcelField<F, *>, Pair<Int, Any?>>()
        excelFields.forEachIndexed { index, excelField ->
            if (excelField.isIndexColumn) {
                return@forEachIndexed
            }
            val column = this.column + index
            val cellValue = row.getCellValue(column, excelField.isDateField)
            notAllBlank = notAllBlank || !excelField.isEmptyCell(cellValue)
            try {
                excelField.setProperty(entity, cellValue, validator, validateGroups)
            } catch (e: Exception) {
                rowErrors.add(
                    CellError(
                        row = this.row,
                        column = column,
                        excelField = excelField,
                        value = cellValue,
                        exception = e
                    )
                )
            }
            val validator = excelField.validator
            if (validator != null) {
                validators[excelField] = column to cellValue
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
                        row = this.row,
                        column = pair.first,
                        excelField = excelField,
                        value = pair.second,
                        exception = e
                    )
                )
            }
        }
        return if (notAllBlank) {
            if (rowErrors.isNotEmpty()) {
                val exception = rowErrors[0].exception
                throw ExcelImportException(exception.message, rowErrors, exception)
            }
            @Suppress("UNCHECKED_CAST")
            converter?.invoke(entity) ?: (entity as E)
        } else {
            null
        }
    }


    /**
     * 获取单元格值
     *
     * @param row    获取的行
     * @param column 获取单元格列号
     * @return 单元格值
     */
    fun Row.getCellValue(column: Int, isDateField: Boolean): Any? {
        return getOptionalCell(column).map {
            when (it.type) {
                CellType.STRING -> it.asString()
                CellType.NUMBER -> if (isDateField) {
                    it.asDate()
                } else {
                    it.asNumber()
                }

                CellType.BOOLEAN -> it.asBoolean()
                else -> it.value
            }
        }.orElse(null)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ExcelImport::class.java)
        private val validator = Validation.buildDefaultValidatorFactory().validator

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

        private fun <F> getEntityType(excelFields: Iterable<ExcelField<F, *>>): Class<F> {
            for (excelField in excelFields) {
                val entityType = excelField.entityType
                if (entityType != null) {
                    return entityType
                }
            }
            throw ExcelException("识别表单类型失败")
        }
    }
}
