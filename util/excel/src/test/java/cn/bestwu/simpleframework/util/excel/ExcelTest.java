package cn.bestwu.simpleframework.util.excel;

import cn.bestwu.lang.util.ArrayUtil;
import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
import cn.bestwu.simpleframework.web.serializer.ICodeService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Wu
 * @since 0.0.1
 */
public class ExcelTest {

  @BeforeEach
  public void setUp() {
    CodeSerializer.setCodeService(new ICodeService() {
      @Override
      public String getName(String codeType, Serializable code) {
        return "codeName";
      }

      @Override
      public Serializable getCode(String codeType, String name) {
        return 123;
      }
    });
  }

  private final ExcelField<DataBean, ?>[] excelFields = ArrayUtil.of(
      ExcelField.of("编码", DataBean::getIntCode),
      ExcelField.of("编码", DataBean::getInteger),
      ExcelField.of("编码", DataBean::getLongl).millis(),
      ExcelField.of("编码", DataBean::getDoublel),
      ExcelField.of("编码", DataBean::getFloatl),
      ExcelField.of("编码", DataBean::getName),
      ExcelField.of("编码", DataBean::getDate)
  );

  @Test
  public void testExport() throws IOException {

    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      DataBean bean = new DataBean();
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    new ExcelExport(new FileOutputStream("build/export.xlsx"), "表格")
        .setData(list, excelFields).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    Runtime.getRuntime().exec("xdg-open " + System.getProperty("user.dir") + "/build/export.xlsx");
  }

  private final ExcelField<DataBean, ?>[] excelMergeFields = ArrayUtil.of(
      ExcelField.mergeId(DataBean::getIntCode),
      ExcelField.of("编码", DataBean::getIntCode),
      ExcelField.of("编码B", DataBean::getInteger).merge(),
      ExcelField.of("名称", from -> new String[]{"abc", "1"}),
      ExcelField.of("描述", DataBean::getName),
      ExcelField.of("描述C", DataBean::getDate).merge()
//      new ExcelField<DataBean, String>().propertySetter(DataBean::setCode).title("编码"),
  );

  @Test
  public void testMergeExport() throws IOException {
    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      DataBean bean = new DataBean();
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    new ExcelExport(new FileOutputStream("build/export.xlsx"), "表格").serialNumber()
        .setData(list, excelMergeFields).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    Runtime.getRuntime().exec("xdg-open " + System.getProperty("user.dir") + "/build/export.xlsx");
  }


  @Test
  public void testImport() throws Exception {
    testExport();
    List<DataBean> list = new ExcelImport(new File("build/export.xlsx"))
        .getData(excelFields);
    System.out.println(StringUtil.valueOf(list, true));
  }

  @Test
  public void testTemplate() throws IOException {
    new ExcelExport(new FileOutputStream("build/template.xlsx"), "表格1").template(excelFields);
    Runtime.getRuntime()
        .exec("xdg-open " + System.getProperty("user.dir") + "/build/template.xlsx");
  }


  public static class DataBean {

    private Integer intCode = 1;
    private Integer integer=2;
    private Long longl=new Date().getTime();
    private double doublel=4.4;
    private float floatl=5.5f;
    private String name="名称";
    private Date date = new Date();


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

    public double getDoublel() {
      return doublel;
    }

    public void setDoublel(double doublel) {
      this.doublel = doublel;
    }

    public float getFloatl() {
      return floatl;
    }

    public void setFloatl(float floatl) {
      this.floatl = floatl;
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