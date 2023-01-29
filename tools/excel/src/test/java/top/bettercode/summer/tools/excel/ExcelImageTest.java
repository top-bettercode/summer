package top.bettercode.summer.tools.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
import top.bettercode.summer.tools.lang.util.ArrayUtil;
import top.bettercode.summer.tools.lang.util.RandomUtil;

/**
 * @author Peter Wu
 * @since 0.0.1
 */
public class ExcelImageTest {

  @Test
  public void imageCellTest() throws Exception {
    XSSFWorkbook wb = new XSSFWorkbook();
    XSSFSheet sheet = wb.createSheet("My Sample Excel");

    //Returns an object that handles instantiating concrete classes
    CreationHelper helper = wb.getCreationHelper();
    //Creates the top-level drawing patriarch.
    Drawing<XSSFShape> drawing = sheet.createDrawingPatriarch();

    //Create an anchor that is attached to the worksheet
    ClientAnchor anchor = helper.createClientAnchor();

    //create an anchor with upper left cell _and_ bottom right cell
    anchor.setCol1(1); //Column B
    anchor.setRow1(2); //Row 3
    anchor.setCol2(2); //Column C
    anchor.setRow2(3); //Row 4

    //FileInputStream obtains input bytes from the image file
    InputStream inputStream = new ClassPathResource("ico.jpeg").getInputStream();
    //Get the contents of an InputStream as a byte[].
    byte[] bytes = IOUtils.toByteArray(inputStream);
    //Adds a picture to the workbook
    //close the input stream
    inputStream.close();
    int pictureIdx = wb.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG);

    //Creates a picture
    drawing.createPicture(anchor, pictureIdx);

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
    FileOutputStream fileOut;
    String filename = "build/excel.xlsx";
    fileOut = new FileOutputStream(filename);
    wb.write(fileOut);
    fileOut.close();

    openExcel(filename);
  }


  private static void openExcel(String x) throws IOException {
//    Runtime.getRuntime().exec(new String[]{"xdg-open", System.getProperty("user.dir") + "/" + x});
  }


  @Test
  public void testMergeExportWithImage() throws IOException {
    ExcelImageCellHandler<DataBean> cellHandler = new ExcelImageCellHandler<>();

    ExcelField<DataBean, ?>[] excelMergeFields = ArrayUtil.of(
        ExcelField.<DataBean, Integer>index("序号"),
        ExcelField.of("编码", DataBean::getIntCode).mergeBy(DataBean::getIntCode),
        ExcelField.of("编码B", DataBean::getInteger).mergeBy(DataBean::getInteger),
        ExcelField.of("名称", from -> new String[]{"abc", "1"}),
        ExcelField.of("描述", DataBean::getName),
        ExcelField.of("描述C", DataBean::getDate),
        ExcelField.of("图片1", cellHandler)
            .mergeBy(DataBean::getIntCode).width(5),
        ExcelField.of("图片2", cellHandler).width(5)
    );

    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      DataBean bean = new DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    String filename = "build/export.xlsx";
    ExcelExport.of(filename).sheet("表格")
        .setMergeData(list, excelMergeFields).finish();
    long e = System.currentTimeMillis();

    cellHandler.setImageGetter(from -> {
      try {
        InputStream inputStream = new ClassPathResource("ico.jpeg").getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);
        inputStream.close();
        return bytes;
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    });
    cellHandler.setImage(filename);
    System.err.println(e - s);

    openExcel(filename);
  }


  public static class DataBean {

    private Integer intCode;
    private Integer integer;
    private Long longl;
    private Double doublel;
    private Float floatl;
    private String name;
    private BigDecimal num;
    private Date date;

    public DataBean() {
      intCode = 1;
      integer = 2;
      longl = new Date().getTime();
      doublel = 4.4;
      floatl = 5.5f;
      num = new BigDecimal("0." + RandomUtil.nextInt(2));
      name = "名称";
      date = new Date();
    }

    public DataBean(Integer index) {
      intCode = 1 + index / 3;
      integer = 2 + index / 2;
      longl = new Date().getTime() + index * 10000;
      doublel = 4.4 + index;
      floatl = 5.5f + index;
      num = new BigDecimal("0." + index);
      name = "名称" + index;
      date = new Date();
    }

    public BigDecimal getNum() {
      return num;
    }

    public void setNum(BigDecimal num) {
      this.num = num;
    }

    public Integer getIntCode() {
      return intCode;
    }

    public void setIntCode(Integer intCode) {
      this.intCode = intCode;
    }

    public Integer getInteger() {
      return integer;
    }

    public void setInteger(Integer integer) {
      this.integer = integer;
    }

    public Long getLongl() {
      return longl;
    }

    public void setLongl(Long longl) {
      this.longl = longl;
    }

    public Double getDoublel() {
      return doublel;
    }

    public DataBean setDoublel(Double doublel) {
      this.doublel = doublel;
      return this;
    }

    public Float getFloatl() {
      return floatl;
    }

    public DataBean setFloatl(Float floatl) {
      this.floatl = floatl;
      return this;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Date getDate() {
      return date;
    }

    public void setDate(Date date) {
      this.date = date;
    }
  }
}