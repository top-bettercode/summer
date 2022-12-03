package top.bettercode.summer.tools.sap.connection.pojo;

import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapField;
import top.bettercode.summer.tools.sap.annotation.SapStructure;

@SapStructure("ET_RETURN")
public class EtReturn {

  /**
   * 消息类型: S 成功,E 错误,W 警告,I 信息,A 中断
   */
  @SapField("TYPE")
  private String type;

  /**
   * 消息文本
   */
  @SapField("MESSAGE")
  private String message;

  /**
   * @return 消息类型: S 成功,E 错误,W 警告,I 信息,A 中断
   */
  public String getType() {
    return this.type;
  }

  /**
   * 设置消息类型: S 成功,E 错误,W 警告,I 信息,A 中断
   *
   * @param type 消息类型: S 成功,E 错误,W 警告,I 信息,A 中断
   * @return 返回消息
   */
  public EtReturn setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @return 消息文本
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * 设置消息文本
   *
   * @param message 消息文本
   * @return 返回消息
   */
  public EtReturn setMessage(String message) {
    this.message = message;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}
