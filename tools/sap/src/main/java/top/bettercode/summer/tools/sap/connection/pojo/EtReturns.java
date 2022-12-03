package top.bettercode.summer.tools.sap.connection.pojo;

import java.util.List;
import org.springframework.util.CollectionUtils;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapTable;

public class EtReturns implements ISapReturn {

  /**
   * 返回消息
   */
  @SapTable("ET_RETURN")
  private List<EtReturn> etReturn;


  /**
   * @return 返回消息
   */
  public List<EtReturn> getEtReturn() {
    return this.etReturn;
  }

  /**
   * 设置返回消息
   *
   * @param etReturn 返回消息
   * @return CostResp
   */
  public EtReturns setEtReturn(List<EtReturn> etReturn) {
    this.etReturn = etReturn;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }

  //--------------------------------------------

  @Override
  public boolean isSuccess() {
    if (CollectionUtils.isEmpty(etReturn)) {
      return false;
    } else {
      return "S".equals(etReturn.get(0).getType());
    }
  }


  @Override
  public String getMessage() {
    if (CollectionUtils.isEmpty(etReturn)) {
      return null;
    } else {
      return etReturn.get(0).getMessage();
    }
  }
}