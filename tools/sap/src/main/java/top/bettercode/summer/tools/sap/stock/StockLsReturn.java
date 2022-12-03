package top.bettercode.summer.tools.sap.stock;

import java.math.BigDecimal;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapField;

public class StockLsReturn {

  /**
   * 可用数量
   */
  @SapField("MENGE_1")
  private BigDecimal menge1 = new BigDecimal("0.000");

  /**
   * 库存数量
   */
  @SapField("MENGE_2")
  private BigDecimal menge2 = new BigDecimal("0.000");

  /**
   * 基本计量单位
   */
  @SapField("MEINS")
  private String meins;

  /**
   * 库存地点
   */
  @SapField("LGORT")
  private String lgort;

  /**
   * @return 可用数量
   */
  public BigDecimal getMenge1() {
    return this.menge1;
  }

  /**
   * 设置可用数量
   *
   * @param menge1 可用数量
   * @return CRM1 与AP返回库存查询数量
   */
  public StockLsReturn setMenge1(BigDecimal menge1) {
    this.menge1 = menge1;
    return this;
  }

  /**
   * @return 库存数量
   */
  public BigDecimal getMenge2() {
    return this.menge2;
  }

  /**
   * 设置库存数量
   *
   * @param menge2 库存数量
   * @return CRM1 与AP返回库存查询数量
   */
  public StockLsReturn setMenge2(BigDecimal menge2) {
    this.menge2 = menge2;
    return this;
  }

  /**
   * @return 基本计量单位
   */
  public String getMeins() {
    return this.meins;
  }

  /**
   * 设置基本计量单位
   *
   * @param meins 基本计量单位
   * @return CRM1 与AP返回库存查询数量
   */
  public StockLsReturn setMeins(String meins) {
    this.meins = meins;
    return this;
  }

  /**
   * @return 库存地点
   */
  public String getLgort() {
    return this.lgort;
  }

  /**
   * 设置库存地点
   *
   * @param lgort 库存地点
   * @return CRM1 与AP返回库存查询数量
   */
  public StockLsReturn setLgort(String lgort) {
    this.lgort = lgort;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}