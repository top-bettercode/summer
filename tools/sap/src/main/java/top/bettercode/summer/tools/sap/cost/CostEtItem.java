package top.bettercode.summer.tools.sap.cost;

import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapField;

public class CostEtItem {

  /**
   * 成本中心
   */
  @SapField("KOSTL")
  private String kostl;

  /**
   * 公司代码
   */
  @SapField("BUKRS")
  private String bukrs;

  /**
   * 长文本
   */
  @SapField("LTEXT")
  private String ltext;

  /**
   * 功能范围
   */
  @SapField("FUNC_AREA")
  private String funcArea;

  /**
   * 功能范围的名称
   */
  @SapField("FKBTX")
  private String fkbtx;

  /**
   * @return 成本中心
   */
  public String getKostl() {
    return this.kostl;
  }

  /**
   * 设置成本中心
   *
   * @param kostl 成本中心
   * @return 成本中心
   */
  public CostEtItem setKostl(String kostl) {
    this.kostl = kostl;
    return this;
  }

  /**
   * @return 公司代码
   */
  public String getBukrs() {
    return this.bukrs;
  }

  /**
   * 设置公司代码
   *
   * @param bukrs 公司代码
   * @return 成本中心
   */
  public CostEtItem setBukrs(String bukrs) {
    this.bukrs = bukrs;
    return this;
  }

  /**
   * @return 长文本
   */
  public String getLtext() {
    return this.ltext;
  }

  /**
   * 设置长文本
   *
   * @param ltext 长文本
   * @return 成本中心
   */
  public CostEtItem setLtext(String ltext) {
    this.ltext = ltext;
    return this;
  }

  /**
   * @return 功能范围
   */
  public String getFuncArea() {
    return this.funcArea;
  }

  /**
   * 设置功能范围
   *
   * @param funcArea 功能范围
   * @return 成本中心
   */
  public CostEtItem setFuncArea(String funcArea) {
    this.funcArea = funcArea;
    return this;
  }

  /**
   * @return 功能范围的名称
   */
  public String getFkbtx() {
    return this.fkbtx;
  }

  /**
   * 设置功能范围的名称
   *
   * @param fkbtx 功能范围的名称
   * @return 成本中心
   */
  public CostEtItem setFkbtx(String fkbtx) {
    this.fkbtx = fkbtx;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}