package top.bettercode.simpleframework.data;

import java.util.List;
import org.springframework.http.ResponseEntity;
import top.bettercode.simpleframework.web.BaseController;
import top.bettercode.simpleframework.web.RespExtra;

/**
 * @author Peter Wu
 */
public class PageController extends BaseController {

  /**
   * @param <T>  T
   * @param list list
   * @return 200 ResponseEntity
   */
  protected <T> ResponseEntity<?> page(List<T> list) {
    return super.ok(BaseService.page(list));
  }

  @Override
  protected  RespExtra<?> of(Object object) {
    if (object instanceof List) {
      return super.of(BaseService.page((List<?>) object));
    } else {
      return super.of(object);
    }
  }

  @Override
  protected ResponseEntity<?> ok(Object object) {
    if (object instanceof PaginationList) {
      return page((PaginationList<?>) object);
    } else {
      return super.ok(object);
    }
  }
}
