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
import top.bettercode.summer.tools.excel.ExcelTestUtil;
import top.bettercode.summer.tools.excel.read.CellGetter;
import top.bettercode.summer.tools.excel.read.ExcelReader;
import top.bettercode.summer.tools.excel.read.RowGetter;
import top.bettercode.summer.tools.excel.write.CellSetter;
import top.bettercode.summer.tools.excel.write.ExcelWriter;
import top.bettercode.summer.tools.excel.write.RowSetter;
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
    Class<?> aClass = CellGetter.getPrimitiveWrapperTypeMap().get(Double.class);
    System.err.println(aClass);
    Assertions.assertEquals(double.class, aClass);
  }

  private final RowSetter<DataBean> rowSetter = RowSetter.of(
          CellSetter.index("序号"),
          CellSetter.of("编码1", DataBean::getIntCode).height(20),
          CellSetter.of("编码2", DataBean::getInteger).comment("批注"),
          CellSetter.of("编码3", DataBean::getLongl),
          CellSetter.of("编码4", DataBean::getDoublel).comment("批注2"),
          CellSetter.of("编码5", DataBean::getFloatl),
          CellSetter.of("编码6", DataBean::getName),
          CellSetter.of("编码7", DataBean::getDate),
          CellSetter.of("编码8", DataBean::getNum));

  @Test
  public void testExport() {

    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      DataBean bean =
          new DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    String filename = "build/testExport" + nameSuff + ".xlsx";
    try (ExcelWriter excelWriter = getExcelWriter(filename)) {
      excelWriter.sheet("表格")
          .setData(list, rowSetter);
    }
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    ExcelTestUtil.openExcel(filename);
  }

  private final RowSetter<DataBean> excelMergeFields =
      RowSetter.of(
          CellSetter.<DataBean, Integer>index("序号")
              .mergeBy(DataBean::getIntCode),
          CellSetter.of("编码", DataBean::getIntCode)
              .mergeBy(DataBean::getIntCode),
          CellSetter.of("编码B", DataBean::getInteger)
              .mergeBy(DataBean::getInteger),
          CellSetter.of("名称", from -> new String[]{"abc", "1"}),
          CellSetter.of("描述", DataBean::getName),
          CellSetter.of("描述C", DataBean::getDate));

  @Test
  public void testMergeExport() {
    List<DataBean> list = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      DataBean bean =
          new DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    String filename = "build/testMergeExport" + nameSuff + ".xlsx";
    try (ExcelWriter excelWriter = getExcelWriter(filename)) {
      excelWriter.sheet("表格")
          .setData(list, excelMergeFields);
    }
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    ExcelTestUtil.openExcel(filename);
  }

  private final RowGetter<DataBean> rowGetter = RowGetter.of(
      CellGetter.of("编码1", DataBean::getIntCode),
      CellGetter.of("编码2", DataBean::getInteger),
      CellGetter.of("编码3", DataBean::getLongl),
      CellGetter.of("编码4", DataBean::getDoublel),
      CellGetter.of("编码5", DataBean::getFloatl),
      CellGetter.of("编码6", DataBean::getName),
      CellGetter.of("编码7", DataBean::getDate),
      CellGetter.of("编码8", DataBean::getNum));


  @Order(1)
  @Test
  public void testImport() throws Exception {
    //    testExport();
    try (ExcelReader excelReader = ExcelReader.of(
        new ClassPathResource("template.xlsx").getInputStream())) {
      excelReader.column(1);
      List<DataBean> list = excelReader.getData(rowGetter);
      System.out.println(StringUtil.json(list, true));
      System.err.println(list.size());
      Assertions.assertEquals(3, list.size());
    }
  }

  @Order(0)
  @Test
  public void testTemplate() {
    String filename = "build/template" + nameSuff + ".xlsx";
    try (ExcelWriter writer = getExcelWriter(filename)) {
      writer
          .sheet("表格1")
          .dataValidation(1, "1", "2", "3")
          .template(rowSetter)
      ;
    }
    ExcelTestUtil.openExcel(filename);
  }

  @NotNull
  protected ExcelWriter getExcelWriter(String filename) {
    return ExcelWriter.of(filename);
  }
}
