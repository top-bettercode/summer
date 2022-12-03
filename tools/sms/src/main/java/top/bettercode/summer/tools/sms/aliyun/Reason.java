package top.bettercode.summer.tools.sms.aliyun;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reason {

  /**
   * 审批未通过的备注信息。
   */
  @JsonProperty("RejectSubInfo")
  private String rejectSubInfo;

  /**
   * 审批未通过的时间，格式为yyyy-MM-dd HH:mm:ss。
   */
  @JsonProperty("RejectDate")
  private String rejectDate;

  /**
   * 审批未通过的原因。
   */
  @JsonProperty("RejectInfo")
  private String rejectInfo;

  public void setRejectSubInfo(String rejectSubInfo) {
    this.rejectSubInfo = rejectSubInfo;
  }

  public String getRejectSubInfo() {
    return rejectSubInfo;
  }

  public void setRejectDate(String rejectDate) {
    this.rejectDate = rejectDate;
  }

  public String getRejectDate() {
    return rejectDate;
  }

  public void setRejectInfo(String rejectInfo) {
    this.rejectInfo = rejectInfo;
  }

  public String getRejectInfo() {
    return rejectInfo;
  }

  @Override
  public String toString() {
    return
        "Reason{" +
            "rejectSubInfo = '" + rejectSubInfo + '\'' +
            ",rejectDate = '" + rejectDate + '\'' +
            ",rejectInfo = '" + rejectInfo + '\'' +
            "}";
  }
}