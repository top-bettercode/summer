package cn.bestwu.simpleframework.support.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

/**
 * 响应结果包装类 可反复取响应内容
 *
 * @author Peter Wu
 */
public class ClientHttpResponseWrapper implements ClientHttpResponse {

  private final ClientHttpResponse response;
  private final byte[] bytes;

  public ClientHttpResponseWrapper(ClientHttpResponse response) throws IOException {
    this.response = response;
    bytes = StreamUtils.copyToByteArray(response.getBody());
  }

  public String getRecord() {
    return new String(bytes);
  }

  @Override
  public HttpHeaders getHeaders() {
    return this.response.getHeaders();
  }

  @Override
  public InputStream getBody() throws IOException {
    return new ByteArrayInputStream(bytes);
  }


  @Override
  public HttpStatus getStatusCode() throws IOException {
    return this.response.getStatusCode();
  }

  @Override
  public int getRawStatusCode() throws IOException {
    return this.response.getRawStatusCode();
  }

  @Override
  public String getStatusText() throws IOException {
    return this.response.getStatusText();
  }

  @Override
  public void close() {
    this.response.close();
  }

}