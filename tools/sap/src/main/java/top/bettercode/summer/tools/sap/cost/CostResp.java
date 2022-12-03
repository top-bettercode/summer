package top.bettercode.summer.tools.sap.cost;

import java.util.List;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapTable;
import top.bettercode.summer.tools.sap.connection.pojo.EtReturns;

public class CostResp extends EtReturns {

  /**
   * 成本中心
   */
  @SapTable("ET_ITEM")
  private List<CostEtItem> etItem;

  /**
   * KOSTL的范围
   */
  @SapTable("SO_KOSTL")
  private List<CostSoKostl> soKostl;

  /**
   * @return 成本中心
   */
  public List<CostEtItem> getEtItem() {
    return this.etItem;
  }

  /**
   * 设置成本中心
   *
   * @param etItem 成本中心
   * @return CostResp
   */
  public CostResp setEtItem(List<CostEtItem> etItem) {
    this.etItem = etItem;
    return this;
  }

  /**
   * @return KOSTL的范围
   */
  public List<CostSoKostl> getSoKostl() {
    return this.soKostl;
  }

  /**
   * 设置KOSTL的范围
   *
   * @param soKostl KOSTL的范围
   * @return CostResp
   */
  public CostResp setSoKostl(List<CostSoKostl> soKostl) {
    this.soKostl = soKostl;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}