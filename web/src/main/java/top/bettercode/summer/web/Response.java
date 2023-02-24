package top.bettercode.summer.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 响应客户端
 *
 * @author Peter Wu
 */
public class Response {

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

  protected RespExtra<?> of(Object object) {
    return new RespExtra<>(object);
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

  protected ResponseEntity<?> message(String status, String message) {
    return ok(new RespEntity<>(status, message));
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
