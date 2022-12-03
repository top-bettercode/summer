package top.bettercode.summer.tools.sap.cost;

import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapField;

public class CostSoKostl {

  /**
   * 借方/贷方符号 (+/-)
   */
  @SapField("SIGN")
  private String sign;

  /**
   * 范围表选项
   */
  @SapField("OPTION")
  private String option;

  /**
   * 成本中心
   */
  @SapField("LOW")
  private String low;

  /**
   * 成本中心
   */
  @SapField("HIGH")
  private String high;

  /**
   * @return 借方/贷方符号 (+/-)
   */
  public String getSign() {
    return this.sign;
  }

  /**
   * 设置借方/贷方符号 (+/-)
   *
   * @param sign 借方/贷方符号 (+/-)
   * @return KOSTL的范围
   */
  public CostSoKostl setSign(String sign) {
    this.sign = sign;
    return this;
  }

  /**
   * @return 范围表选项
   */
  public String getOption() {
    return this.option;
  }

  /**
   * 设置范围表选项
   *
   * @param option 范围表选项
   * @return KOSTL的范围
   */
  public CostSoKostl setOption(String option) {
    this.option = option;
    return this;
  }

  /**
   * @return 成本中心
   */
  public String getLow() {
    return this.low;
  }

  /**
   * 设置成本中心
   *
   * @param low 成本中心
   * @return KOSTL的范围
   */
  public CostSoKostl setLow(String low) {
    this.low = low;
    return this;
  }

  /**
   * @return 成本中心
   */
  public String getHigh() {
    return this.high;
  }

  /**
   * 设置成本中心
   *
   * @param high 成本中心
   * @return KOSTL的范围
   */
  public CostSoKostl setHigh(String high) {
    this.high = high;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}