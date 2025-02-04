package top.bettercode.summer.tools.java;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import top.bettercode.summer.tools.excel.write.ExcelWriter;

/**
 * @author Peter Wu
 */
public class PoiExcelTest extends ExcelTest {

  public PoiExcelTest() {
    nameSuff = "2";
  }

  @NotNull
  @Override
  protected ExcelWriter getExcelWriter(String filename) {
    return ExcelWriter.of(filename, true);
  }

  @Test
  @Override
  public void testExport() {
    super.testExport();
  }

  @Test
  @Override
  public void testMergeExport() {
    super.testMergeExport();
  }

  @Test
  @Override
  public void testTemplate() {
    super.testTemplate();
  }
}
