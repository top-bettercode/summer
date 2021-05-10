package cn.bestwu.simpleframework.support.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.ResponseExtractor;

/**
 * Response extractor for {@link HttpEntity}.
 */
public class ResponseEntityResponseTraceExtractor<T> implements
    ResponseExtractor<ResponseEntity<T>> {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final HttpMessageConverterExtractor<T> delegate;

  public ResponseEntityResponseTraceExtractor(Type responseType,
      List<HttpMessageConverter<?>> messageConverters) {
    if (responseType != null && Void.class != responseType) {
      this.delegate = new HttpMessageConverterExtractor<>(responseType, messageConverters);
    } else {
      this.delegate = null;
    }
  }

  @Override
  public ResponseEntity<T> extractData(ClientHttpResponse response) throws IOException {
    if (this.delegate != null) {
      if (log.isInfoEnabled()) {
        response = new ClientHttpResponseWrapper(response);
        log.info("response:{}", ((ClientHttpResponseWrapper) response).getRecord());
      }
      T body = this.delegate.extractData(response);
      return ResponseEntity.status(response.getRawStatusCode()).headers(response.getHeaders())
          .body(body);
    } else {
      return null;
    }
  }
}