package top.bettercode.summer.tools.sms.b2m;

import java.util.List;
import top.bettercode.summer.tools.lang.property.PropertiesSource;
import top.bettercode.summer.tools.sms.SmsResponse;

/**
 * @author Peter Wu
 */
public class B2mResponse<T> implements SmsResponse {

  public static final String SUCCESS = "SUCCESS";
  private static final PropertiesSource codeMessageSource = PropertiesSource.of("b2m-message");
  private String code;

  private List<T> data;

  public B2mResponse() {
  }

  public B2mResponse(List<T> data) {
    this.code = SUCCESS;
    this.data = data;
  }

  //--------------------------------------------

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public List<T> getData() {
    return data;
  }

  public void setData(List<T> data) {
    this.data = data;
  }

  //--------------------------------------------

  @Override
  public boolean isOk() {
    return SUCCESS.equals(code);
  }

  @Override
  public String getMessage() {
    return codeMessageSource.getOrDefault(code, code);
  }

  //--------------------------------------------

  public static String getMessage(String code) {
    return codeMessageSource.getOrDefault(code, code);
  }
}
