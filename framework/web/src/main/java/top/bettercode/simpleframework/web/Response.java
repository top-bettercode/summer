package top.bettercode.simpleframework.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 响应客户端
 *
 * @author Peter Wu
 */
public class Response {

  /**
   * @param headers 响应头
   * @return 不支持支持客户端缓存，支持客户端保存数据的响应头，即支持必须revalidate的缓存
   */
  public static HttpHeaders cacheControl(HttpHeaders headers) {
    if (headers == null) {
      headers = new HttpHeaders();
    }
    headers.setCacheControl("no-cache, must-revalidate");
    headers.setPragma("no-cache");
    headers.setExpires(-1);
    return headers;
  }

  /**
   * 成功创建资源
   *
   * @param resource resource
   * @return 201 ResponseEntity
   */
  protected ResponseEntity<?> created(Object resource) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(resource);
  }

  /**
   * 成功更新资源
   *
   * @param resource resource
   * @return 200 ResponseEntity
   */
  protected ResponseEntity<?> updated(Object resource) {
    return ok(resource);
  }

  /**
   * @param object object
   * @return 200 ResponseEntity
   */
  protected ResponseEntity<?> ok(Object object) {
    return ResponseEntity.ok().body(object);
  }

  /**
   * @param message message
   * @return 200 ResponseEntity
   */
  protected ResponseEntity<?> message(String message) {
    return ok(new RespEntity<>(String.valueOf(HttpStatus.OK.value()), message));
  }

  /**
   * @param message message
   * @return 400 ResponseEntity
   */
  protected ResponseEntity<?> errorMessage(String message) {
    return ok(new RespEntity<>(String.valueOf(HttpStatus.BAD_REQUEST.value()), message));
  }

  /**
   * 响应空白内容
   *
   * @return 204
   */
  protected ResponseEntity<?> noContent() {
    return ResponseEntity.noContent().build();
  }

}
