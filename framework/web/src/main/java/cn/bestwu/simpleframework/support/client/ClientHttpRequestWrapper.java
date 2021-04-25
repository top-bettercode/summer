package cn.bestwu.simpleframework.support.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @author Peter Wu
 */
public class ClientHttpRequestWrapper implements ClientHttpRequest {

  private Logger log = LoggerFactory.getLogger(ClientHttpRequestWrapper.class);
  private final ClientHttpRequest request;
  private final ByteArrayOutputStream record = new ByteArrayOutputStream();

  public ClientHttpRequestWrapper(ClientHttpRequest request) {
    this.request = request;
  }

  @Override
  public ClientHttpResponse execute() throws IOException {
    ClientHttpResponse response = request.execute();
    if (log.isDebugEnabled()) {
      log.debug("request heads:{}",request.getHeaders());
      log.debug("request:{}", record.toString());
    }
    return response;
  }

  @Override
  public HttpMethod getMethod() {
    return request.getMethod();
  }

  @Override
  public String getMethodValue() {
    return request.getMethodValue();
  }

  @Override
  public URI getURI() {
    return request.getURI();
  }

  @Override
  public HttpHeaders getHeaders() {
    return request.getHeaders();
  }

  @Override
  public OutputStream getBody() throws IOException {
    return new OutputStreamWrapper(request.getBody());
  }

  private class OutputStreamWrapper extends OutputStream {

    private final OutputStream delegate;


    private OutputStreamWrapper(OutputStream delegate) {
      this.delegate = delegate;
    }

    @Override
    public void write(int b) throws IOException {
      delegate.write(b);
      record.write(b);
    }
  }
}
