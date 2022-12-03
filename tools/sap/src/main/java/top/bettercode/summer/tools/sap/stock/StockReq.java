package top.bettercode.summer.tools.sap.stock;

import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapField;
import top.bettercode.summer.tools.sap.annotation.SapStructure;
import top.bettercode.summer.tools.sap.connection.pojo.SapHead;

public class StockReq {

  /**
   * 接口控制数据
   */
  @SapStructure("IS_ZSCRM2_CONTROL")
  private SapHead head;

  /**
   * 物料号
   */
  @SapField("I_MATERIAL")
  private String iMaterial;

  /**
   * 工厂
   */
  @SapField("I_PLANT")
  private String iPlant;

  /**
   * 库存地点
   */
  @SapField("I_STGE_LOC")
  private String iStgeLoc;

  /**
   * @return 接口控制数据
   */
  public SapHead getHead() {
    return this.head;
  }

  /**
   * 设置接口控制数据
   *
   * @param head 接口控制数据
   * @return StockReq
   */
  public StockReq setHead(SapHead head) {
    this.head = head;
    return this;
  }

  /**
   * @return 物料号
   */
  public String getIMaterial() {
    return this.iMaterial;
  }

  /**
   * 设置物料号
   *
   * @param iMaterial 物料号
   * @return StockReq
   */
  public StockReq setIMaterial(String iMaterial) {
    this.iMaterial = iMaterial;
    return this;
  }

  /**
   * @return 工厂
   */
  public String getIPlant() {
    return this.iPlant;
  }

  /**
   * 设置工厂
   *
   * @param iPlant 工厂
   * @return StockReq
   */
  public StockReq setIPlant(String iPlant) {
    this.iPlant = iPlant;
    return this;
  }

  /**
   * @return 库存地点
   */
  public String getIStgeLoc() {
    return this.iStgeLoc;
  }

  /**
   * 设置库存地点
   *
   * @param iStgeLoc 库存地点
   * @return StockReq
   */
  public StockReq setIStgeLoc(String iStgeLoc) {
    this.iStgeLoc = iStgeLoc;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}