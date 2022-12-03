package top.bettercode.summer.tools.sap.stock;

import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapStructure;
import top.bettercode.summer.tools.sap.connection.pojo.RkEsMessage;
import top.bettercode.summer.tools.sap.connection.pojo.SapReturn;

public class StockResp extends SapReturn<RkEsMessage> {

  /**
   * CRM1 与AP返回库存查询数量
   */
  @SapStructure("LS_RETURN")
  private StockLsReturn lsReturn;

  /**
   * @return CRM1 与AP返回库存查询数量
   */
  public StockLsReturn getLsReturn() {
    return this.lsReturn;
  }

  /**
   * 设置CRM1 与AP返回库存查询数量
   *
   * @param lsReturn CRM1 与AP返回库存查询数量
   * @return StockResp
   */
  public StockResp setLsReturn(StockLsReturn lsReturn) {
    this.lsReturn = lsReturn;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}