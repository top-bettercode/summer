package cn.bestwu.simpleframework.support;

import java.math.BigDecimal;

/**
 * @author Peter Wu
 */
public class KilogramUtil {

  /**
   * @param kilogram 单位千克
   * @return 单位克
   */
  public static Long toGram(String kilogram) {
    return new BigDecimal(kilogram).setScale(3, BigDecimal.ROUND_HALF_UP)
        .multiply(new BigDecimal(1000)).longValue();
  }

  /**
   * @param gram 单位克
   * @return 单位千克
   */
  public static BigDecimal toKilogram(long gram) {
    return toKilogram(new BigDecimal(gram), 3);
  }

  /**
   * @param gram 单位克
   * @param scale 小数位数
   * @return 单位千克
   */
  public static BigDecimal toKilogram(BigDecimal gram, int scale) {
    return gram.divide(new BigDecimal(1000), scale, BigDecimal.ROUND_HALF_UP);
  }

}
