package top.bettercode.summer.tools.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.bettercode.summer.tools.lang.util.ArrayUtil;
import top.bettercode.summer.tools.lang.util.StringUtil;

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
      ExcelField.of("编码3", DataBean::getLongl),
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
    openExcel("build/export.xlsx");
  }

  private static void openExcel(String x) throws IOException {
    Runtime.getRuntime().exec(new String[]{"xdg-open", System.getProperty("user.dir") + "/" + x});
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
    for (int i = 0; i < 22; i++) {
      DataBean bean = new DataBean(i);
      list.add(bean);
    }
    long s = System.currentTimeMillis();
    ExcelExport.of("build/export.xlsx").sheet("表格")
        .setMergeData(list, excelMergeFields).finish();
    long e = System.currentTimeMillis();
    System.err.println(e - s);
    openExcel("build/export.xlsx");
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
    openExcel("build/template.xlsx");
  }
}