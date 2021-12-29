package top.bettercode.sms.aliyun;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AliSmsResponse {

  @JsonProperty("RequestId")
  private String requestId;

  @JsonProperty("BizId")
  private String bizId;

  @JsonProperty("Code")
  private String code;

  @JsonProperty("Message")
  private String message;

  public String getRequestId() {
    return this.requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getBizId() {
    return this.bizId;
  }

  public void setBizId(String bizId) {
    this.bizId = bizId;
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

  public boolean isOk() {
    return "OK".equals(code);
  }
}
