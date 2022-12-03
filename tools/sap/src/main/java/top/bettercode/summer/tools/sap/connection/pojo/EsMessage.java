package top.bettercode.summer.tools.sap.connection.pojo;

import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapField;

public class EsMessage {

  /**
   * 消息类型 S:成功；E:错误；W:警告
   */
  @SapField("MTYPE")
  private String type;

  /**
   * 消息描述 字符100
   */
  @SapField("MSGTXT")
  private String message;

  public String getType() {
    return type;
  }

  public EsMessage setType(String type) {
    this.type = type;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public EsMessage setMessage(String message) {
    this.message = message;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }

  //--------------------------------------------

  public boolean isOk() {
    return StringUtils.hasText(type) && !"E".equals(type);
  }

  public boolean isSuccess() {
    return "S".equals(type);
  }

}
