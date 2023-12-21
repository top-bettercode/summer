package top.bettercode.summer.tools.java;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import top.bettercode.summer.tools.excel.ExcelConverter;
import top.bettercode.summer.tools.excel.ExcelExport;
import top.bettercode.summer.tools.excel.ExcelField;
import top.bettercode.summer.tools.excel.ExcelTestUtil;
import top.bettercode.summer.tools.lang.util.ArrayUtil;

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
    InputStream inputStream = new ClassPathResource("ico.jpeg").getInputStream();
    // Get the contents of an InputStream as a byte[].
    byte[] bytes = IOUtils.toByteArray(inputStream);
    // Adds a picture to the workbook
    // close the input stream
    inputStream.close();
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

    ExcelConverter<top.bettercode.summer.tools.java.DataBean, InputStream> excelConverter =
        from -> {
          try {
            return new ClassPathResource("ico.jpeg").getInputStream();
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
        };
    ExcelField<top.bettercode.summer.tools.java.DataBean, ?>[] excelMergeFields =
        ArrayUtil.of(
            ExcelField.<top.bettercode.summer.tools.java.DataBean, Integer>index("序号"),
            ExcelField.of("编码", top.bettercode.summer.tools.java.DataBean::getIntCode)
                .mergeBy(top.bettercode.summer.tools.java.DataBean::getIntCode),
            ExcelField.of("编码B", top.bettercode.summer.tools.java.DataBean::getInteger)
                .mergeBy(top.bettercode.summer.tools.java.DataBean::getInteger),
            ExcelField.of("名称", from -> new String[]{"abc", "1"}),
            ExcelField.of("描述", top.bettercode.summer.tools.java.DataBean::getName),
            ExcelField.of("描述C", top.bettercode.summer.tools.java.DataBean::getDate),
            ExcelField.image("图片1", excelConverter)
                .mergeBy(top.bettercode.summer.tools.java.DataBean::getIntCode)
                .width(10),
            ExcelField.image("图片2", excelConverter).width(10).height(40));

    List<top.bettercode.summer.tools.java.DataBean> list = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      top.bettercode.summer.tools.java.DataBean bean =
          new top.bettercode.summer.tools.java.DataBean(i);
      list.add(bean);
    }
    for (int i = 22; i < 25; i++) {
      top.bettercode.summer.tools.java.DataBean bean =
          new top.bettercode.summer.tools.java.DataBean();
      bean.setIntCode(i);
      list.add(bean);
    }
    list.add(new top.bettercode.summer.tools.java.DataBean(25));
    list.add(new DataBean(25));
    long s = System.currentTimeMillis();

    String filename = "build/testMergeExportWithImage.xlsx";

    ExcelExport.of(filename, true)
        .sheet("表格")
        .setMergeData(list, excelMergeFields)
        .finish();
    long e = System.currentTimeMillis();

    System.err.println(e - s);

    ExcelTestUtil.openExcel(filename);
  }
}
