package top.bettercode.summer.tools.excel

import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.excel.write.ExcelWriter

/**
 *
 * @author Peter Wu
 */
class PoiExcelTest : ExcelTest() {
    override var nameSuff: String = "2"

    override fun excelExport(filename: String): ExcelWriter {
        return ExcelWriter.of(filename, true)
    }

    @Test
    override fun testExport() {
        super.testExport()
    }

    @Test
    override fun testMergeExport() {
        super.testMergeExport()
    }

    @Test
    override fun testTemplate() {
        super.testTemplate()
    }
}