package top.bettercode.summer.tools.excel

import org.junit.jupiter.api.*
import org.springframework.core.io.ClassPathResource
import top.bettercode.summer.tools.excel.read.CellGetter
import top.bettercode.summer.tools.excel.read.ExcelReader
import top.bettercode.summer.tools.excel.read.RowGetter
import top.bettercode.summer.tools.excel.write.CellSetter
import top.bettercode.summer.tools.excel.write.ExcelWriter
import top.bettercode.summer.tools.excel.write.RowSetter
import top.bettercode.summer.tools.lang.util.StringUtil.json

/**
 * @author Peter Wu
 * @since 0.0.1
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
open class ExcelTest {

    protected open var nameSuff: String = "1"

    private val rowSetter = RowSetter.of(
        CellSetter.index<DataBean, Any?>("序号"),
        CellSetter.of("编码1", DataBean::intCode),
        CellSetter.of("编码2", DataBean::integer).comment("批注"),
        CellSetter.of("编码3", DataBean::longl),
        CellSetter.of("编码4", DataBean::doublel).comment("批注2"),
        CellSetter.of("编码5", DataBean::floatl),
        CellSetter.of("编码6", DataBean::name),
        CellSetter.of("编码7") { obj: DataBean -> obj.date },
        CellSetter.of("编码8") { obj: DataBean -> obj.num }
    )

    @Test
    open fun testExport() {
        val list: MutableList<DataBean> = ArrayList()
        for (i in 0..7) {
            val bean = DataBean(i)
            list.add(bean)
        }
        val s = System.currentTimeMillis()
        val filename = "build/testExport$nameSuff.xlsx"
        excelExport(filename).use {
            it.sheet("表格")
                .setData(list, rowSetter)
        }
        val e = System.currentTimeMillis()
        System.err.println(e - s)
        ExcelTestUtil.openExcel(filename)
    }

    private val rowSetter2 = RowSetter.of(
        CellSetter.index<DataBean, Int?>("序号").mergeBy { obj: DataBean -> obj.intCode },
        CellSetter.of("编码") { obj: DataBean -> obj.intCode }
            .mergeBy { obj: DataBean -> obj.intCode },
        CellSetter.of("编码B") { obj: DataBean -> obj.integer }
            .mergeBy { obj: DataBean -> obj.integer },
        CellSetter.of("名称") { arrayOf("abc", "1") },
        CellSetter.of("描述") { obj: DataBean -> obj.name },
        CellSetter.of("描述C") { obj: DataBean -> obj.date }
    )

    @Test
    open fun testMergeExport() {
        val list: MutableList<DataBean> = ArrayList()
        for (i in 0..21) {
            val bean = DataBean(i)
            list.add(bean)
        }
        val s = System.currentTimeMillis()
        val filename = "build/testMergeExport$nameSuff.xlsx"
        excelExport(filename).use {
            it.sheet("表格")
                .setData(list, rowSetter2)
        }
        val e = System.currentTimeMillis()
        System.err.println(e - s)
        ExcelTestUtil.openExcel(filename)
    }

    private val rowGetter  = RowGetter.of(
        CellGetter.of("编码1", DataBean::intCode),
        CellGetter.of("编码2", DataBean::integer),
        CellGetter.of("编码3", DataBean::longl),
        CellGetter.of("编码4", DataBean::doublel),
        CellGetter.of("编码5", DataBean::floatl),
        CellGetter.of("编码6", DataBean::name)
    )

    @Order(1)
    @Test
    fun testImport() {
        ExcelReader.of(ClassPathResource("template.xlsx").inputStream).use {
            it.column(1)
            val list = it.getData<DataBean, DataBean>(rowGetter)
            println(json(list, true))
            System.err.println(list.size)
            Assertions.assertEquals(3, list.size)
        }
    }

    @Order(0)
    @Test
    open fun testTemplate() {
        val filename = "build/template$nameSuff.xlsx"
        excelExport(filename).use {
            it.sheet("表格1").dataValidation(1, "1", "2", "3")
                .template(rowSetter)
        }
        ExcelTestUtil.openExcel(filename)
    }

    protected open fun excelExport(filename: String) = ExcelWriter.of(filename)

}