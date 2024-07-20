package top.bettercode.summer.tools.excel

import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.RichTextString
import org.apache.poi.xssf.usermodel.XSSFRichTextString
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.excel.write.CellSetter
import top.bettercode.summer.tools.excel.write.ExcelWriter
import top.bettercode.summer.tools.excel.write.RowSetter

/**
 * @author Peter Wu
 * @since 0.0.1
 */
class ExcelPoiTest {
    @Test
    fun testRichText() {
        val excelMergeFields = RowSetter.of(
            CellSetter.index<DataBean, Int?>("序号"),
            CellSetter.of("编码") { obj: DataBean -> obj.intCode }
                .mergeBy { obj: DataBean -> obj.intCode },
            CellSetter.of("编码B") { obj: DataBean -> obj.integer }
                .mergeBy { obj: DataBean -> obj.integer },
            CellSetter.of("名称") { _: DataBean -> arrayOf("abc", "1") },
            CellSetter.of("描述") { obj: DataBean -> obj.name },
            CellSetter.of("描述C") { obj: DataBean -> obj.date }.setter { excel, cell ->
                // 设置文本
                val text = "Hello, World!"
                // 创建字体
                val sheet = excel.sheet
                val poiCell = sheet.getRow(cell.row).getCell(cell.column)
                // 创建字体
                val boldFont = excel.xssfWorkbook.createFont()
                boldFont.bold = true
                boldFont.color = Font.COLOR_RED
                boldFont.fontHeight = 120

                // 创建样式
//                    val boldStyle = workbook.createCellStyle()
//                    boldStyle.setFont(boldFont)

                // 在单元格中添加部分文本并应用样式

                // 在单元格中添加部分文本并应用样式
                val richText: RichTextString = XSSFRichTextString(text)
                richText.applyFont(0, 5, boldFont) // 应用样式到部分文本

                poiCell.setCellValue(richText)

            }.mergeBy { obj: DataBean -> obj.intCode },
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
        ExcelWriter.of(filename = filename, poi = true, useSxss = false).use {
            it.sheet("表格")
                .setData(list, excelMergeFields)
        }
        val e = System.currentTimeMillis()
        System.err.println(e - s)
        ExcelTestUtil.openExcel(filename)
    }

}