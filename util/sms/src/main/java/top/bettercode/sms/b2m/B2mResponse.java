package top.bettercode.sms.b2m;

import java.util.List;
import top.bettercode.lang.property.PropertiesSource;
import top.bettercode.sms.SmsResponse;

/**
 * @author Peter Wu
 */
public class B2mResponse implements SmsResponse {

  public static final String SUCCESS = "SUCCESS";
  private static final PropertiesSource codeMessageSource = PropertiesSource.of("b2m-message");
  private String code;

  private List<B2mRespData> data;

  public B2mResponse() {
  }

  public B2mResponse(List<B2mRespData> data) {
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

  public List<B2mRespData> getData() {
    return data;
  }

  public void setData(List<B2mRespData> data) {
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
