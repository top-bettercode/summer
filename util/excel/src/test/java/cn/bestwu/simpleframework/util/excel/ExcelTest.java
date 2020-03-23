package cn.bestwu.simpleframework.util.excel;

import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.simpleframework.util.excel.converter.CodeConverter;
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

  @Test
  public void testExport() throws IOException {
    List<DataBean2> list = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      list.add(new DataBean2("name" + i, "中文中文中文中文中文中文中文中文" + i));
    }
    long s = System.currentTimeMillis();
    new ExcelExport(new FileOutputStream("build/export.xlsx"), "表格", DataBean3.class)
        .setDataList(list).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    Runtime.getRuntime().exec("xdg-open " + System.getProperty("user.dir") + "/build/export.xlsx");
  }

  @Test
  public void testExport2() throws IOException {
    List<DataBean2> list = new ArrayList<>();
    list.add(new DataBean2("name1", "desc1"));
    list.add(new DataBean2("中文", "desc2"));
    list.add(new DataBean2("name3", "desc3"));
    list.add(new DataBean2("name4", "desc4"));
    long s = System.currentTimeMillis();
    new ExcelExport(new FileOutputStream("build/export.xlsx"), "表格", DataBean3.class)
        .setDataList(list).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    new ExcelExport(new FileOutputStream("build/export1.xlsx"), "表格", DataBean3.class)
        .setDataList(list).finish();
    Runtime.getRuntime().exec("xdg-open " + System.getProperty("user.dir") + "/build/export.xlsx");
  }


  @Test
  public void testImport() throws Exception {
    testExport();
    List<DataBean3> list = new ExcelImport(new File("build/export.xlsx"))
        .getDataList(DataBean3.class);
    System.out.println(StringUtil.valueOf(list, true));
//    Assert.assertEquals(3L, list.size());
  }

  @Test
  public void testTemplate() throws IOException {
    new ExcelExport(new FileOutputStream("build/template.xlsx"), "表格1", DataBean3.class).template();
    Runtime.getRuntime()
        .exec("xdg-open " + System.getProperty("user.dir") + "/build/template.xlsx");
  }

  public static class DataBean3 extends DataBean {

    public DataBean3() {
    }

    public DataBean3(String name, String desc) {
      super(name, desc);
    }

    @ExcelField(title = "描述中文中文", sort = 1, comment = "描述\naaa")
    @Override
    public String getDesc() {
      return super.getDesc();
    }

//    @ExcelField(title = "名称", comment = "名称")
    @Override
    public String getName() {
      return super.getName();
    }
  }

  public static class DataBean2 extends DataBean {

    public DataBean2() {
    }

    public DataBean2(String name, String desc) {
      super(name, desc);
    }

  }

  public static class DataBean {

    private Integer intCode = 1;
    private String code = "";
    private String name;
    private String desc;
    private String remark;
    private Integer a;
    private Integer b;
    private Integer c;
    private Date date = new Date();

    public DataBean() {
    }

    public DataBean(String name, String desc) {
      this.name = name;
      this.desc = desc;
    }

    @ExcelField(title = "编码1", converter = CodeConverter.class, sort = 2, comment = "实体编码")
    public Integer getIntCode() {
      return intCode;
    }

    public void setIntCode(Integer intCode) {
      this.intCode = intCode;
    }

    @ExcelField(title = "编码2", sort = 3)
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

    @ExcelField(title = "a", sort = 4)
    public Integer getA() {
      return a;
    }

    public void setA(Integer a) {
      this.a = a;
    }

    @ExcelField(title = "b", sort = 5)
    public Integer getB() {
      return b;
    }

    public void setB(Integer b) {
      this.b = b;
    }

    @ExcelField(title = "c", sort = 6)
    public Integer getC() {
      return c;
    }

    public void setC(Integer c) {
      this.c = c;
    }

    @ExcelField(title = "日期", sort = 7)
    public Date getDate() {
      return date;
    }

    public void setDate(Date date) {
      this.date = date;
    }

    @ExcelField(title = "备注", sort = 8)
    public String getRemark() {
      return remark;
    }
    public void setRemark(String remark) {
      this.remark = remark;
    }
  }
}