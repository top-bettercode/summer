package top.bettercode.summer.tools.sms.aliyun;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AliSmsSignResponse extends AliSmsResponse {

  @JsonProperty("SmsSignList")
  private List<SmsSignListItem> smsSignList;

  public void setSmsSignList(List<SmsSignListItem> smsSignList) {
    this.smsSignList = smsSignList;
  }

  public List<SmsSignListItem> getSmsSignList() {
    return smsSignList;
  }

  @Override
  public String toString() {
    return "AliSmsSignResp{" +
        "smsSignList=" + smsSignList +
        "} " + super.toString();
  }
}