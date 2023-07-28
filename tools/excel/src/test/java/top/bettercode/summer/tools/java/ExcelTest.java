package top.bettercode.summer.tools.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
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

  @BeforeEach
  public void setUp() {}

  @Test
  void testPrimitive() {
    Class<?> aClass = ExcelField.Companion.getPrimitiveWrapperTypeMap().get(Double.class);
    System.err.println(aClass);
    Assertions.assertEquals(double.class, aClass);
  }

  private final ExcelField<DataBean, ?>[] excelFields =
      ArrayUtil.of(
          ExcelField.index("序号"),
          ExcelField.of("编码1", DataBean::getIntCode),
          ExcelField.of("编码2", DataBean::getInteger),
          ExcelField.of("编码3", DataBean::getLongl),
          ExcelField.of("编码4", DataBean::getDoublel),
          ExcelField.of("编码5", DataBean::getFloatl),
          ExcelField.of("编码6", DataBean::getName),
          ExcelField.of("编码7", DataBean::getDate),
          ExcelField.of("编码8", DataBean::getNum));

  @Test
  public void testExport() throws IOException {

    List<top.bettercode.summer.tools.java.DataBean> list = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      top.bettercode.summer.tools.java.DataBean bean =
          new top.bettercode.summer.tools.java.DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    ExcelExport.of("build/testExport.xlsx").sheet("表格").setData(list, excelFields).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    ExcelTestUtil.openExcel("build/testExport.xlsx");
  }

  private final ExcelField<top.bettercode.summer.tools.java.DataBean, ?>[] excelMergeFields =
      ArrayUtil.of(
          ExcelField.<top.bettercode.summer.tools.java.DataBean, Integer>index("序号")
              .mergeBy(top.bettercode.summer.tools.java.DataBean::getIntCode),
          ExcelField.of("编码", top.bettercode.summer.tools.java.DataBean::getIntCode)
              .mergeBy(top.bettercode.summer.tools.java.DataBean::getIntCode),
          ExcelField.of("编码B", top.bettercode.summer.tools.java.DataBean::getInteger)
              .mergeBy(top.bettercode.summer.tools.java.DataBean::getInteger),
          ExcelField.of("名称", from -> new String[] {"abc", "1"}),
          ExcelField.of("描述", top.bettercode.summer.tools.java.DataBean::getName),
          ExcelField.of("描述C", top.bettercode.summer.tools.java.DataBean::getDate));

  @Test
  public void testMergeExport() throws IOException {
    List<top.bettercode.summer.tools.java.DataBean> list = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      top.bettercode.summer.tools.java.DataBean bean =
          new top.bettercode.summer.tools.java.DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    ExcelExport.of("build/testMergeExport.xlsx")
        .sheet("表格")
        .setMergeData(list, excelMergeFields)
        .finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    ExcelTestUtil.openExcel("build/testMergeExport.xlsx");
  }

  @Order(1)
  @Test
  public void testImport() throws Exception {
    //    testExport();
    List<DataBean> list =
        ExcelImport.of(new ClassPathResource("template.xlsx").getInputStream())
            .getData(excelFields);
    System.out.println(StringUtil.valueOf(list, true));
    System.err.println(list.size());
  }

  @Order(0)
  @Test
  public void testTemplate() throws IOException {
    ExcelExport.of("build/template.xlsx")
        .sheet("表格1")
        .dataValidation(1, "1,2,3")
        .template(excelFields)
        .finish();
    ExcelTestUtil.openExcel("build/template.xlsx");
  }
}
