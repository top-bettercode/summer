package top.bettercode.summer.tools.sap.connection.pojo;

import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapStructure;

/**
 * 返回sap实体
 */
public class SapReturn<T extends EsMessage> implements ISapReturn {

  @SapStructure("ES_MESSAGE")
  private T esMessage;

  public T getEsMessage() {
    return esMessage;
  }

  public SapReturn<T> setEsMessage(T esMessage) {
    this.esMessage = esMessage;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
  //--------------------------------------------

  @Override
  public boolean isOk() {
    return this.esMessage.isOk();
  }

  @Override
  public boolean isSuccess() {
    return this.esMessage.isSuccess();
  }

  @Override
  public String getMessage() {
    return this.esMessage.getMessage();
  }
}
