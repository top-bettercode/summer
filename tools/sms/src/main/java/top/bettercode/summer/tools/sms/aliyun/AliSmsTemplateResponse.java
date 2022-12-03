package top.bettercode.summer.tools.sms.aliyun;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AliSmsTemplateResponse extends AliSmsResponse {

  @JsonProperty("SmsTemplateList")
  private List<SmsTemplateListItem> smsTemplateList;

  public void setSmsTemplateList(List<SmsTemplateListItem> smsTemplateList) {
    this.smsTemplateList = smsTemplateList;
  }

  public List<SmsTemplateListItem> getSmsTemplateList() {
    return smsTemplateList;
  }

  @Override
  public String toString() {
    return
        "AliSmsTemplateResponse{" +
            "smsTemplateList = '" + smsTemplateList + '\'' +
            "}";
  }
}