package top.bettercode.summer.tools.sap.connection.pojo;

import top.bettercode.summer.tools.lang.util.StringUtil;
import top.bettercode.summer.tools.sap.annotation.SapField;
import top.bettercode.summer.tools.sap.annotation.SapStructure;

@SapStructure("ES_MESSAGE")
public class RkEsMessage extends EsMessage {

  /**
   * 接收数据键值 长度为 40 的字符型字段
   */
  @SapField("RKDATA")
  private String rkdata;

  //--------------------------------------------

  /**
   * @return 长度为 40 的字符型字段
   */
  public String getRkdata() {
    return this.rkdata;
  }

  /**
   * 设置长度为 40 的字符型字段
   *
   * @param rkdata 长度为 40 的字符型字段
   * @return 返回消息结构
   */
  public RkEsMessage setRkdata(String rkdata) {
    this.rkdata = rkdata;
    return this;
  }

  @Override
  public String toString() {
    return StringUtil.json(this);
  }
}
