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
import org.junit.Before;
import org.junit.Test;

/**
 * @author Peter Wu
 * @since 0.0.1
 */
public class ExcelTest {

  @Before
  public void setUp() throws Exception {
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
//      ExcelField.of("编码", DataBean::getA),
      ExcelField.of("名称", from -> new String[]{"abc", "1"}),
      ExcelField.of("描述", from -> from.getDesc())
//      new ExcelField<DataBean, String>().propertySetter(DataBean::setCode).title("编码"),
  );

  @Test
  public void testExport() throws IOException {

    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      list.add(new DataBean(i + 1L, "name" + i, "中文中文中文中文中文中文中文中文" + i));
    }
    long s = System.currentTimeMillis();
    new ExcelExport(new FileOutputStream("build/export.xlsx"), "表格")
        .setData(list, excelFields).finish();
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
    private String code = "";
    private String name;
    private String desc;
    private String remark;
    private Long a;
    private Integer b;
    private Integer c;
    private Date date = new Date();

    public DataBean() {
    }

    public DataBean(Long a, String name, String desc) {
      this.a = a;
      this.name = name;
      this.desc = desc;
    }

    public Integer getIntCode() {
      return intCode;
    }

    public void setIntCode(Integer intCode) {
      this.intCode = intCode;
    }

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDesc() {
      return desc;
    }

    public void setDesc(String desc) {
      this.desc = desc;
    }

    public Long getA() {
      return a;
    }

    public void setA(Long a) {
      this.a = a;
    }

    public Integer getB() {
      return b;
    }

    public void setB(Integer b) {
      this.b = b;
    }

    public Integer getC() {
      return c;
    }

    public void setC(Integer c) {
      this.c = c;
    }

    public Date getDate() {
      return date;
    }

    public void setDate(Date date) {
      this.date = date;
    }

    public String getRemark() {
      return remark;
    }

    public void setRemark(String remark) {
      this.remark = remark;
    }
  }
}