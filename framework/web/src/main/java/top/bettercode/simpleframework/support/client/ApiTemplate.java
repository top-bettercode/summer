package top.bettercode.simpleframework.support.client;

import java.io.IOException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import top.bettercode.logging.client.ClientHttpRequestWrapper;

/**
 * zabbix请求模板
 *
 * @author Peter Wu
 */
public class ApiTemplate extends RestTemplate {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final String collectionName;
  private final String name;
  private final String logMarker;

  public ApiTemplate(int connectTimeout, int readTimeout) {
    this("", "", null, connectTimeout, readTimeout);
  }

  public ApiTemplate(String collectionName, String name, int connectTimeout, int readTimeout) {
    this(collectionName, name, null, connectTimeout, readTimeout);
  }

  public ApiTemplate(String collectionName, String name, String logMarker, int connectTimeout,
      int readTimeout) {
    this.collectionName = collectionName;
    this.name = name;
    this.logMarker = logMarker;

    SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
    //Connect timeout
    clientHttpRequestFactory.setConnectTimeout(connectTimeout);
    //Read timeout
    clientHttpRequestFactory.setReadTimeout(readTimeout);
    setRequestFactory(clientHttpRequestFactory);
  }

  @Override
  protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
    if (log.isInfoEnabled()) {
      return new ClientHttpRequestWrapper(collectionName, name, logMarker,
          super.createRequest(url, method));
    } else {
      return super.createRequest(url, method);
    }
  }

}
