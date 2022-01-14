package top.bettercode.sms;

import kotlin.jvm.functions.Function1;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import top.bettercode.simpleframework.support.client.ApiTemplate;

/**
 * @author Peter Wu
 */
public abstract class SmsTemplate extends ApiTemplate {

  protected static final String LOG_MARKER_STR = "sms";
  public static final Marker LOG_MARKER = MarkerFactory.getMarker(LOG_MARKER_STR);


  public SmsTemplate(String collectionName, String name, String logMarker, int connectTimeout,
      int readTimeout, Function1<byte[], byte[]> decrypt) {
    super(collectionName, name, logMarker, connectTimeout, readTimeout, decrypt);
  }
}
