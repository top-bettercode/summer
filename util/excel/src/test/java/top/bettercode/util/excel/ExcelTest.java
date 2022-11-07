package top.bettercode.util.excel;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.bettercode.lang.util.ArrayUtil;
import top.bettercode.lang.util.RandomUtil;
import top.bettercode.lang.util.StringUtil;

/**
 * @author Peter Wu
 * @since 0.0.1
 */
public class ExcelTest {

  @BeforeEach
  public void setUp() {

  }

  private final ExcelField<DataBean, ?>[] excelFields = ArrayUtil.of(
      ExcelField.index("序号"),
      ExcelField.of("编码1", DataBean::getIntCode),
      ExcelField.of("编码2", DataBean::getInteger),
      ExcelField.of("编码3", DataBean::getLongl).millis(),
      ExcelField.of("编码4", DataBean::getDoublel),
      ExcelField.of("编码5", DataBean::getFloatl),
      ExcelField.of("编码6", DataBean::getName),
      ExcelField.of("编码7", DataBean::getDate),
      ExcelField.of("编码8", DataBean::getNum)
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
    Runtime.getRuntime()
        .exec(new String[]{"xdg-open", System.getProperty("user.dir") + "/build/export.xlsx"});
  }

  private final ExcelField<DataBean, ?>[] excelMergeFields = ArrayUtil.of(
      ExcelField.<DataBean, Integer>index("序号"),
      ExcelField.of("编码", DataBean::getIntCode).mergeBy(DataBean::getIntCode),
      ExcelField.of("编码B", DataBean::getInteger).mergeBy(DataBean::getInteger),
      ExcelField.of("名称", from -> new String[]{"abc", "1"}),
      ExcelField.of("描述", DataBean::getName),
      ExcelField.of("描述C", DataBean::getDate)
  );

  @Test
  public void testMergeExport() throws IOException {
    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      DataBean bean = new DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    ExcelExport.of("build/export.xlsx").sheet("表格")
        .setMergeData(list, excelMergeFields).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    Runtime.getRuntime()
        .exec(new String[]{"xdg-open", System.getProperty("user.dir") + "/build/export.xlsx"});
  }


  @Test
  public void testImport() throws Exception {
//    testExport();
    List<DataBean> list = ExcelImport.of("build/template.xlsx").setColumn(1)
        .getData(excelFields);
    System.out.println(StringUtil.valueOf(list, true));
    System.err.println(list.size());
  }

  @Test
  public void testTemplate() throws IOException {
    ExcelExport.of("build/template.xlsx").sheet("表格1").dataValidation(1, "1,2,3")
        .template(excelFields)
        .finish();
    Runtime.getRuntime()
        .exec(new String[]{"xdg-open", System.getProperty("user.dir") + "/build/template.xlsx"});
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