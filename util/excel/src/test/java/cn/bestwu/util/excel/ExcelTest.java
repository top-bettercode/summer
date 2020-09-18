package cn.bestwu.util.excel;

import cn.bestwu.lang.util.ArrayUtil;
import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.simpleframework.support.code.ICodeService;
import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
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
      ExcelField.index("序号"),
      ExcelField.of("编码1", DataBean::getIntCode),
      ExcelField.of("编码2", DataBean::getInteger),
      ExcelField.of("编码3", DataBean::getLongl).millis(),
      ExcelField.of("编码4", DataBean::getDoublel),
      ExcelField.of("编码5", DataBean::getFloatl),
      ExcelField.of("编码6", DataBean::getName),
      ExcelField.of("编码7", DataBean::getDate)
  );

  @Test
  public void testExport() throws IOException {

    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      DataBean bean = new DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    ExcelExport.of("build/export.xlsx").sheet("表格")
        .setData(list, excelFields).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    Runtime.getRuntime().exec("xdg-open " + System.getProperty("user.dir") + "/build/export.xlsx");
  }

  private final ExcelField<DataBean, ?>[] excelMergeFields = ArrayUtil.of(
      ExcelField.<DataBean, Integer>index("序号").mergeBy(DataBean::getIntCode),
      ExcelField.of("编码", DataBean::getIntCode).mergeBy(DataBean::getIntCode),
      ExcelField.of("编码B", DataBean::getInteger).mergeBy(DataBean::getInteger),
      ExcelField.of("名称", from -> new String[]{"abc", "1"}),
      ExcelField.of("描述", DataBean::getName),
      ExcelField.of("描述C", DataBean::getDate)
  );

  @Test
  public void testMergeExport() throws IOException {
    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      DataBean bean = new DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    ExcelExport.of("build/export.xlsx").sheet("表格")
        .setMergeData(list, excelMergeFields).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    Runtime.getRuntime().exec("xdg-open " + System.getProperty("user.dir") + "/build/export.xlsx");
  }


  @Test
  public void testImport() throws Exception {
    testExport();
    List<DataBean> list = ExcelImport.of("build/export.xlsx")
        .getData(excelFields);
    System.out.println(StringUtil.valueOf(list, true));
  }

  @Test
  public void testTemplate() throws IOException {
    ExcelExport.of("build/template.xlsx").sheet("表格1").template(excelFields).finish();
    Runtime.getRuntime()
        .exec("xdg-open " + System.getProperty("user.dir") + "/build/template.xlsx");
  }


  public static class DataBean {

    private Integer intCode;
    private Integer integer;
    private Long longl;
    private double doublel;
    private float floatl;
    private String name;
    private Date date;

    public DataBean() {
      intCode = 1;
      integer = 2;
      longl = new Date().getTime();
      doublel = 4.4;
      floatl = 5.5f;
      name = "名称";
      date = new Date();
    }

    public DataBean(Integer index) {
      intCode = 1 + index / 3;
      integer = 2 + index / 2;
      longl = new Date().getTime() + index * 10000;
      doublel = 4.4 + index;
      floatl = 5.5f + index;
      name = "名称" + index;
      date = new Date();
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