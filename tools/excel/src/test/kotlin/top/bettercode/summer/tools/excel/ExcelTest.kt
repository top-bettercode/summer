package top.bettercode.summer.tools.excel

import org.junit.jupiter.api.*
import org.springframework.core.io.ClassPathResource
import top.bettercode.summer.tools.lang.util.StringUtil.valueOf
import java.io.IOException

/**
 * @author Peter Wu
 * @since 0.0.1
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ExcelTest {
    @BeforeEach
    fun setUp() {
    }

    private val excelFields: Array<ExcelField<DataBean, *>> = arrayOf(
            ExcelField.index<DataBean, Any?>("序号"),
            ExcelField.of("编码1", DataBean::intCode),
            ExcelField.of("编码2", DataBean::integer),
            ExcelField.of("编码3", DataBean::longl),
            ExcelField.of("编码4", DataBean::doublel),
            ExcelField.of("编码5", DataBean::floatl),
            ExcelField.of("编码6", DataBean::name),
            ExcelField.of("编码7") { obj: DataBean -> obj.date },
            ExcelField.of("编码8") { obj: DataBean -> obj.num }
    )

    @Test
    fun testExport() {
        val list: MutableList<DataBean> = ArrayList()
        for (i in 0..7) {
            val bean = DataBean(i)
            list.add(bean)
        }
        val s = System.currentTimeMillis()
        ExcelExport.of("build/testExport.xlsx").sheet("表格")
                .setData(list, excelFields).finish()
        val e = System.currentTimeMillis()
        System.err.println(e - s)
        ExcelTestUtil.openExcel("build/testExport.xlsx")
    }

    private val excelMergeFields: Array<ExcelField<DataBean, *>> = arrayOf(
            ExcelField.index<DataBean, Int?>("序号").mergeBy { obj: DataBean -> obj.intCode },
            ExcelField.of("编码") { obj: DataBean -> obj.intCode }.mergeBy { obj: DataBean -> obj.intCode },
            ExcelField.of("编码B") { obj: DataBean -> obj.integer }.mergeBy { obj: DataBean -> obj.integer },
            ExcelField.of("名称") { arrayOf("abc", "1") },
            ExcelField.of("描述") { obj: DataBean -> obj.name },
            ExcelField.of("描述C") { obj: DataBean -> obj.date }
    )

    @Test
    fun testMergeExport() {
        val list: MutableList<DataBean> = ArrayList()
        for (i in 0..21) {
            val bean = DataBean(i)
            list.add(bean)
        }
        val s = System.currentTimeMillis()
        ExcelExport.of("build/testMergeExport.xlsx").sheet("表格")
                .setMergeData(list, excelMergeFields).finish()
        val e = System.currentTimeMillis()
        System.err.println(e - s)
        ExcelTestUtil.openExcel("build/testMergeExport.xlsx")
    }

    @Order(1)
    @Test
    fun testImport() {
//    testExport();
        val list = ExcelImport.of(ClassPathResource("template.xlsx").inputStream)
                .getData<DataBean, DataBean>(excelFields)
        println(valueOf(list, true))
        System.err.println(list.size)
    }

    @Order(0)
    @Test
    fun testTemplate() {
        ExcelExport.of("build/template.xlsx").sheet("表格1").dataValidation(1, "1,2,3")
                .template(excelFields)
                .finish()
        ExcelTestUtil.openExcel("build/template.xlsx")
    }

}