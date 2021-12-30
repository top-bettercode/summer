package top.bettercode.sms;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import top.bettercode.simpleframework.support.client.ApiTemplate;

/**
 * @author Peter Wu
 */
public abstract class SmsTemplate extends ApiTemplate {

  protected static final String LOG_MARKER_STR = "sms";
  public static final Marker LOG_MARKER = MarkerFactory.getMarker(LOG_MARKER_STR);

  public SmsTemplate(int connectTimeout, int readTimeout) {
    super(connectTimeout, readTimeout);
  }

  public SmsTemplate(String collectionName, String name, int connectTimeout, int readTimeout) {
    super(collectionName, name, connectTimeout, readTimeout);
  }

  public SmsTemplate(String collectionName, String name, String logMarker, int connectTimeout,
      int readTimeout) {
    super(collectionName, name, logMarker, connectTimeout, readTimeout);
  }
}
