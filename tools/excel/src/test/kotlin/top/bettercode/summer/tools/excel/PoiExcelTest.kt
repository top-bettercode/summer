package top.bettercode.summer.tools.excel

import org.junit.jupiter.api.Test

/**
 *
 * @author Peter Wu
 */
class PoiExcelTest : ExcelTest() {
    override var nameSuff: String = "2"

    override fun excelExport(filename: String): ExcelExport {
        return ExcelExport.of(filename, true)
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