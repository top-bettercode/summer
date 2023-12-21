package top.bettercode.summer.tools.excel

import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.RichTextString
import org.apache.poi.xssf.usermodel.XSSFRichTextString
import org.junit.jupiter.api.Test

/**
 * @author Peter Wu
 * @since 0.0.1
 */
class ExcelPoiTest {
    @Test
    fun testRichText() {
        val excelMergeFields: Array<ExcelField<DataBean, *>> = arrayOf(
                ExcelField.index<DataBean, Int?>("序号"),
                ExcelField.of("编码") { obj: DataBean -> obj.intCode }.mergeBy { obj: DataBean -> obj.intCode },
                ExcelField.of("编码B") { obj: DataBean -> obj.integer }.mergeBy { obj: DataBean -> obj.integer },
                ExcelField.of("名称") { _: DataBean -> arrayOf("abc", "1") },
                ExcelField.of("描述") { obj: DataBean -> obj.name },
                ExcelField.poi("描述C", { obj: DataBean -> obj.date }, { excel, cell ->
                    // 设置文本
                    val text = "Hello, World!"
                    // 创建字体
                    val sheet = excel.sheet
                    val xssfCell = excel.sheet.getRow(cell.row).getCell(cell.column)
                    // 创建字体
                    val boldFont: Font = sheet.workbook.createFont()
                    boldFont.bold = true
                    boldFont.color = Font.COLOR_RED
                    boldFont.fontHeight = 120

                    // 创建样式

                    // 创建样式
//                    val boldStyle = workbook.createCellStyle()
//                    boldStyle.setFont(boldFont)

                    // 在单元格中添加部分文本并应用样式

                    // 在单元格中添加部分文本并应用样式
                    val richText: RichTextString = XSSFRichTextString(text)
                    richText.applyFont(0, 5, boldFont) // 应用样式到部分文本

                    xssfCell.setCellValue(richText)

                }).mergeBy { obj: DataBean -> obj.intCode },
        )
        val list: MutableList<DataBean> = ArrayList()
        for (i in 0..21) {
            val bean = DataBean(i)
            list.add(bean)
        }
        for (i in 22..24) {
            val bean = DataBean()
            bean.intCode = i
            list.add(bean)
        }
        list.add(DataBean(25))
        list.add(DataBean(25))
        val s = System.currentTimeMillis()
        val filename = "build/testMergeExportWithPoi.xlsx"
        ExcelExport.of(filename, true).sheet("表格")
                .setMergeData(list, excelMergeFields)
                .finish()
        val e = System.currentTimeMillis()
        System.err.println(e - s)
        ExcelTestUtil.openExcel(filename)
    }

}