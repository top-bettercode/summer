package cn.bestwu.simpleframework.util.excel;

import cn.bestwu.lang.util.CharUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Wu
 */
public class ColumnWidths {

  private final int maxWidth;
  private final Map<Integer, Double> colWidths = new HashMap<>();

  public ColumnWidths() {
    this(50);
  }

  public ColumnWidths(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public void put(Integer column, Object val) {
    if (val != null) {
      double width = 0;
      for (char c1 : val.toString().toCharArray()) {
        if (CharUtil.isChinese(c1)) {
          width += 1.5;
        } else {
          width += 1;
        }
      }
      width += +20.0 / 7;
      width = Math
          .max(colWidths.getOrDefault(column, 0.0), width);
      colWidths.put(column, width);
    }
  }

  public Double width(Integer column) {
    return width(column, maxWidth);
  }

  public Double width(Integer column, Integer max) {
    return BigDecimal.valueOf(Math.min(max, colWidths.get(column)))
        .setScale(2, BigDecimal.ROUND_HALF_UP)
        .doubleValue();
  }

}
