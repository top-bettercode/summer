package top.bettercode.summer.tools.excel

import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Drawing
import org.apache.poi.xssf.usermodel.XSSFShape
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import top.bettercode.summer.tools.excel.ExcelTestUtil.numberImage
import top.bettercode.summer.tools.excel.write.CellSetter
import top.bettercode.summer.tools.excel.write.ExcelWriter
import top.bettercode.summer.tools.excel.write.RowSetter
import java.io.FileOutputStream

/**
 * @author Peter Wu
 * @since 0.0.1
 */
class ExcelImageTest {
    @Test
    fun imageCellTest() {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("My Sample Excel")

        //Returns an object that handles instantiating concrete classes
        val helper: CreationHelper = wb.creationHelper
        //Creates the top-level drawing patriarch.
        val drawing: Drawing<XSSFShape> = sheet.createDrawingPatriarch()

        //Create an anchor that is attached to the worksheet
        val anchor = helper.createClientAnchor()

        //create an anchor with upper left cell _and_ bottom right cell
        anchor.setCol1(1) //Column B
        anchor.row1 = 2 //Row 3
        anchor.setCol2(2) //Column C
        anchor.row2 = 3 //Row 4

        //FileInputStream obtains input bytes from the image file
        //Get the contents of an InputStream as a byte[].
        val bytes = numberImage(1)
        //Adds a picture to the workbook
        //close the input stream
        val pictureIdx = wb.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG)

        //Creates a picture
        drawing.createPicture(anchor, pictureIdx)

        //Reset the image to the original size
        //pict.resize(); //don't do that. Let the anchor resize the image!

        //Create the Cell B3
//    Cell cell = sheet.createRow(2).createCell(1);

        //set width to n character widths = count characters * 256
        //int widthUnits = 20*256;
        //sheet.setColumnWidth(1, widthUnits);

        //set height to n points in twips = n * 20
        //short heightUnits = 60*20;
        //cell.getRow().setHeight(heightUnits);

        //Write the Excel file
        val fileOut: FileOutputStream
        val filename = "build/imageCellTest.xlsx"
        fileOut = FileOutputStream(filename)
        wb.write(fileOut)
        fileOut.close()
        ExcelTestUtil.openExcel(filename)
    }

    @Test
    fun testMergeExportWithImage() {
        val rowSetter = RowSetter.of(
            CellSetter.index<DataBean, Int?>("序号").height(40.0),
            CellSetter.of("编码") { obj: DataBean -> obj.intCode }
                .mergeBy { obj: DataBean -> obj.intCode },
            CellSetter.of("编码B") { obj: DataBean -> obj.integer }
                .mergeBy { obj: DataBean -> obj.integer },
            CellSetter.of("名称") { _: DataBean -> arrayOf("abc", "1") },
            CellSetter.of("描述") { obj: DataBean -> obj.name },
            CellSetter.of("描述C") { obj: DataBean -> obj.date },
            CellSetter.image("图片1") { obj: DataBean -> numberImage(obj.intCode) }
                .mergeBy { obj: DataBean -> obj.intCode }
                .width(10.0),
            CellSetter.image("图片2") { obj: DataBean -> numberImage(obj.integer2) }.width(10.0)
        )
        val list: MutableList<DataBean> = ArrayList()
        for (i in 0..21) {
            val bean = DataBean(i)
            list.add(bean)
        }
        for (i in 22..24) {
            val bean = DataBean()
            bean.intCode = i
            bean.integer2 = i
            list.add(bean)
        }
        list.add(DataBean(25))
        list.add(DataBean(25))
        val s = System.currentTimeMillis()
        val filename = "build/testMergeExportWithImage.xlsx"
        ExcelWriter.of(filename, true).use {
            it.sheet("表格").setData(list, rowSetter)
        }
        val e = System.currentTimeMillis()
        System.err.println(e - s)
        ExcelTestUtil.openExcel(filename)
    }

}