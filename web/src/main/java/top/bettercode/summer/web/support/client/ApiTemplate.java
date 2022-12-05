package top.bettercode.summer.web.support.client;

import java.io.IOException;
import java.net.URI;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import top.bettercode.summer.tools.lang.client.ClientHttpRequestWrapper;

/**
 * zabbix请求模板
 *
 * @author Peter Wu
 */
public class ApiTemplate extends RestTemplate {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());
  private final String collectionName;
  private final String name;
  protected final String logMarker;
  private final Function1<byte[], byte[]> requestDecrypt;
  private final Function1<byte[], byte[]> responseDecrypt;

  public ApiTemplate(int connectTimeout, int readTimeout) {
    this("", "", connectTimeout, readTimeout);
  }

  public ApiTemplate(String collectionName, String name, int connectTimeout, int readTimeout) {
    this(collectionName, name, null, connectTimeout, readTimeout);
  }

  public ApiTemplate(String collectionName, String name, String logMarker, int connectTimeout,
      int readTimeout) {
    this(collectionName, name, logMarker, connectTimeout, readTimeout, null, null);
  }

  public ApiTemplate(String collectionName, String name, String logMarker, int connectTimeout,
      int readTimeout, Function1<byte[], byte[]> requestDecrypt,
      Function1<byte[], byte[]> responseDecrypt) {
    this.collectionName = collectionName;
    this.name = name;
    this.logMarker = logMarker;
    this.requestDecrypt = requestDecrypt;
    this.responseDecrypt = responseDecrypt;

    SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
    //Connect timeout
    clientHttpRequestFactory.setConnectTimeout(connectTimeout);
    //Read timeout
    clientHttpRequestFactory.setReadTimeout(readTimeout);
    clientHttpRequestFactory.setOutputStreaming(false);
    setRequestFactory(clientHttpRequestFactory);
  }

  @NotNull
  @Override
  protected ClientHttpRequest createRequest(@NotNull URI url, @NotNull HttpMethod method)
      throws IOException {
    if (log.isInfoEnabled()) {
      return new ClientHttpRequestWrapper(collectionName, name, logMarker,
          super.createRequest(url, method), requestDecrypt, responseDecrypt);
    } else {
      return super.createRequest(url, method);
    }
  }

}
