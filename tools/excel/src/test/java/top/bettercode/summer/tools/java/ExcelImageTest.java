package top.bettercode.summer.tools.java;

import static top.bettercode.summer.tools.excel.ExcelTestUtil.numberImage;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import top.bettercode.summer.tools.excel.ExcelTestUtil;
import top.bettercode.summer.tools.excel.write.CellSetter;
import top.bettercode.summer.tools.excel.write.ExcelWriter;
import top.bettercode.summer.tools.excel.write.RowSetter;

/**
 * @author Peter Wu
 * @since 0.0.1
 */
public class ExcelImageTest {

  @Test
  public void imageCellTest() throws Exception {
    XSSFWorkbook wb = new XSSFWorkbook();
    XSSFSheet sheet = wb.createSheet("My Sample Excel");

    // Returns an object that handles instantiating concrete classes
    CreationHelper helper = wb.getCreationHelper();
    // Creates the top-level drawing patriarch.
    Drawing<XSSFShape> drawing = sheet.createDrawingPatriarch();

    // Create an anchor that is attached to the worksheet
    ClientAnchor anchor = helper.createClientAnchor();

    // create an anchor with upper left cell _and_ bottom right cell
    anchor.setCol1(1); // Column B
    anchor.setRow1(2); // Row 3
    anchor.setCol2(2); // Column C
    anchor.setRow2(3); // Row 4

    // FileInputStream obtains input bytes from the image file
    // Get the contents of an InputStream as a byte[].
    byte[] bytes = numberImage(1);
    // Adds a picture to the workbook
    // close the input stream
    int pictureIdx = wb.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG);

    // Creates a picture
    drawing.createPicture(anchor, pictureIdx);

    // Reset the image to the original size
    // pict.resize(); //don't do that. Let the anchor resize the image!

    // Create the Cell B3
    //    Cell cell = sheet.createRow(2).createCell(1);

    // set width to n character widths = count characters * 256
    // int widthUnits = 20*256;
    // sheet.setColumnWidth(1, widthUnits);

    // set height to n points in twips = n * 20
    // short heightUnits = 60*20;
    // cell.getRow().setHeight(heightUnits);

    // Write the Excel file
    FileOutputStream fileOut;
    String filename = "build/imageCellTest.xlsx";
    fileOut = new FileOutputStream(filename);
    wb.write(fileOut);
    fileOut.close();

    ExcelTestUtil.openExcel(filename);
  }

  @Test
  public void testMergeExportWithImage() {

    RowSetter<DataBean> rowSetter = RowSetter.of(
            CellSetter.<DataBean, Integer>index("序号").height(40),
            CellSetter.of("编码", DataBean::getIntCode)
                .mergeBy(DataBean::getIntCode),
            CellSetter.of("编码B", DataBean::getInteger)
                .mergeBy(DataBean::getInteger),
            CellSetter.of("名称", from -> new String[]{"abc", "1"}),
            CellSetter.of("描述", DataBean::getName),
            CellSetter.of("描述C", DataBean::getDate),
            CellSetter.<DataBean, byte[]>image("图片1",
                    from -> numberImage(from.getIntCode()))
                .mergeBy(DataBean::getIntCode)
                .width(10),
            CellSetter.<DataBean, byte[]>image("图片2",
                from -> numberImage(from.getInteger2())).width(10));

    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      DataBean bean =
          new DataBean(i);
      list.add(bean);
    }
    for (int i = 22; i < 25; i++) {
      DataBean bean =
          new DataBean();
      bean.setIntCode(i);
      list.add(bean);
    }
    list.add(new DataBean(25));
    list.add(new DataBean(25));
    long s = System.currentTimeMillis();

    String filename = "build/testMergeExportWithImage.xlsx";

    try (ExcelWriter writer = ExcelWriter.of(filename, true)) {
      writer
          .sheet("表格")
          .setData(list, rowSetter)
      ;
    }
    long e = System.currentTimeMillis();

    System.err.println(e - s);

    ExcelTestUtil.openExcel(filename);
  }
}
