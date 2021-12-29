package top.bettercode.sms.aliyun;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class QuerySendDetailsResponse {

  @JsonProperty("RequestId")
  private String requestId;

  @JsonProperty("Code")
  private String code;

  @JsonProperty("Message")
  private String message;

  @JsonProperty("TotalCount")
  private String totalCount;

  @JsonProperty("SmsSendDetailDTOs")
  private Map<String,List<SmsSendDetailDTO>> smsSendDetailDTOs;

  public String getRequestId() {
    return this.requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getCode() {
    return this.code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTotalCount() {
    return this.totalCount;
  }

  public void setTotalCount(String totalCount) {
    this.totalCount = totalCount;
  }

  public Map<String, List<SmsSendDetailDTO>> getSmsSendDetailDTOs() {
    return smsSendDetailDTOs;
  }

  public void setSmsSendDetailDTOs(
      Map<String, List<SmsSendDetailDTO>> smsSendDetailDTOs) {
    this.smsSendDetailDTOs = smsSendDetailDTOs;
  }

  public boolean isOk() {
    return "OK".equals(code);
  }

  public static class SmsSendDetailDTO {

    /**
     * 接收短信的手机号码
     */
    @JsonProperty("PhoneNum")
    private String phoneNum;

    /**
     * 短信发送状态，包括：
     * <p>
     * 1：等待回执。 2：发送失败。 3：发送成功。
     */
    @JsonProperty("SendStatus")
    private Long sendStatus;

    /**
     * 运营商短信状态码。
     * <p>
     * 短信发送成功：DELIVERED。 短信发送失败：失败错误码请参见错误码。
     */
    @JsonProperty("ErrCode")
    private String errCode;

    /**
     * 短信模板ID。
     */
    @JsonProperty("TemplateCode")
    private String templateCode;

    /**
     * 短信内容
     */
    @JsonProperty("Content")
    private String content;

    /**
     * 短信发送日期和时间
     */
    @JsonProperty("SendDate")
    private String sendDate;
    /**
     * 短信接收日期和时间
     */
    @JsonProperty("ReceiveDate")
    private String receiveDate;

    /**
     * 外部流水扩展字段
     */
    @JsonProperty("OutId")
    private String outId;

    public String getPhoneNum() {
      return this.phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
      this.phoneNum = phoneNum;
    }

    public Long getSendStatus() {
      return this.sendStatus;
    }

    public void setSendStatus(Long sendStatus) {
      this.sendStatus = sendStatus;
    }

    public String getErrCode() {
      return this.errCode;
    }

    public void setErrCode(String errCode) {
      this.errCode = errCode;
    }

    public String getTemplateCode() {
      return this.templateCode;
    }

    public void setTemplateCode(String templateCode) {
      this.templateCode = templateCode;
    }

    public String getContent() {
      return this.content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getSendDate() {
      return this.sendDate;
    }

    public void setSendDate(String sendDate) {
      this.sendDate = sendDate;
    }

    public String getReceiveDate() {
      return this.receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
      this.receiveDate = receiveDate;
    }

    public String getOutId() {
      return this.outId;
    }

    public void setOutId(String outId) {
      this.outId = outId;
    }
  }

}
