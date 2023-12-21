package top.bettercode.summer.tools.java;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.ClassPathResource;
import top.bettercode.summer.tools.excel.ExcelExport;
import top.bettercode.summer.tools.excel.ExcelField;
import top.bettercode.summer.tools.excel.ExcelImport;
import top.bettercode.summer.tools.excel.ExcelTestUtil;
import top.bettercode.summer.tools.lang.util.ArrayUtil;
import top.bettercode.summer.tools.lang.util.StringUtil;

/**
 * @author Peter Wu
 * @since 0.0.1
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExcelTest {

  protected String nameSuff = "1";

  @BeforeEach
  public void setUp() {
  }

  @Test
  void testPrimitive() {
    Class<?> aClass = ExcelField.Companion.getPrimitiveWrapperTypeMap().get(Double.class);
    System.err.println(aClass);
    Assertions.assertEquals(double.class, aClass);
  }

  private final ExcelField<DataBean, ?>[] excelFields =
      ArrayUtil.of(
          ExcelField.index("序号"),
          ExcelField.of("编码1", DataBean::getIntCode).height(20),
          ExcelField.of("编码2", DataBean::getInteger).comment("批注"),
          ExcelField.of("编码3", DataBean::getLongl),
          ExcelField.of("编码4", DataBean::getDoublel).comment("批注2"),
          ExcelField.of("编码5", DataBean::getFloatl),
          ExcelField.of("编码6", DataBean::getName),
          ExcelField.of("编码7", DataBean::getDate),
          ExcelField.of("编码8", DataBean::getNum));

  @Test
  public void testExport() {

    List<top.bettercode.summer.tools.java.DataBean> list = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      top.bettercode.summer.tools.java.DataBean bean =
          new top.bettercode.summer.tools.java.DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    String filename = "build/testExport" + nameSuff + ".xlsx";
    getExcelExport(filename).sheet("表格").setData(list, excelFields).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    ExcelTestUtil.openExcel(filename);
  }

  private final ExcelField<top.bettercode.summer.tools.java.DataBean, ?>[] excelMergeFields =
      ArrayUtil.of(
          ExcelField.<top.bettercode.summer.tools.java.DataBean, Integer>index("序号")
              .mergeBy(top.bettercode.summer.tools.java.DataBean::getIntCode),
          ExcelField.of("编码", top.bettercode.summer.tools.java.DataBean::getIntCode)
              .mergeBy(top.bettercode.summer.tools.java.DataBean::getIntCode),
          ExcelField.of("编码B", top.bettercode.summer.tools.java.DataBean::getInteger)
              .mergeBy(top.bettercode.summer.tools.java.DataBean::getInteger),
          ExcelField.of("名称", from -> new String[]{"abc", "1"}),
          ExcelField.of("描述", top.bettercode.summer.tools.java.DataBean::getName),
          ExcelField.of("描述C", top.bettercode.summer.tools.java.DataBean::getDate));

  @Test
  public void testMergeExport() {
    List<top.bettercode.summer.tools.java.DataBean> list = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      top.bettercode.summer.tools.java.DataBean bean =
          new top.bettercode.summer.tools.java.DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    String filename = "build/testMergeExport" + nameSuff + ".xlsx";
    getExcelExport(filename)
        .sheet("表格")
        .setMergeData(list, excelMergeFields)
        .finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    ExcelTestUtil.openExcel(filename);
  }

  @Order(1)
  @Test
  public void testImport() throws Exception {
    //    testExport();
    List<DataBean> list =
        ExcelImport.of(new ClassPathResource("template.xlsx").getInputStream())
            .getData(excelFields);
    System.out.println(StringUtil.json(list, true));
    System.err.println(list.size());
  }

  @Order(0)
  @Test
  public void testTemplate() {
    String filename = "build/template" + nameSuff + ".xlsx";
    getExcelExport(filename)
        .sheet("表格1")
        .dataValidation(1, "1", "2", "3")
        .template(excelFields)
        .finish();
    ExcelTestUtil.openExcel(filename);
  }

  @NotNull
  protected ExcelExport getExcelExport(String filename) {
    return ExcelExport.of(filename);
  }
}
