package top.bettercode.sms.b2m;

import java.util.List;
import top.bettercode.lang.property.PropertiesSource;

/**
 * @author Peter Wu
 */
public class B2mResponse {

  public static final String SUCCESS="SUCCESS";
  private static final PropertiesSource codeMessageSource = PropertiesSource.of("b2m-message");
  private String code;

  private List<B2mRespData> data;

  public static String getMessage(String code) {
    return codeMessageSource.getOrDefault(code, code);
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public List<B2mRespData> getData() {
    return data;
  }

  public void setData(List<B2mRespData> data) {
    this.data = data;
  }

  public boolean isOk() {
    return SUCCESS.equals(code);
  }

  public String getMessage() {
    return codeMessageSource.getOrDefault(code, code);
  }
}
