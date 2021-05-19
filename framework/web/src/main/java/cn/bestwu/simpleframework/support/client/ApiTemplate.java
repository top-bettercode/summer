package cn.bestwu.simpleframework.support.client;

import cn.bestwu.logging.client.ClientHttpRequestWrapper;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * zabbix请求模板
 *
 * @author Peter Wu
 */
public class ApiTemplate extends RestTemplate {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final String collectionName;
  private final String name;


  public ApiTemplate(int connectTimeout, int readTimeout) {
    this("", "", connectTimeout, readTimeout);
  }

  public ApiTemplate(String collectionName,
      String name, int connectTimeout, int readTimeout) {
    this.collectionName = collectionName;
    this.name = name;
    MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
    messageConverter.getObjectMapper().setDefaultPropertyInclusion(Include.NON_NULL);
    setMessageConverters(Collections.singletonList(messageConverter));

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
      return new ClientHttpRequestWrapper(collectionName, name, super.createRequest(url, method));
    } else {
      return super.createRequest(url, method);
    }
  }

}
