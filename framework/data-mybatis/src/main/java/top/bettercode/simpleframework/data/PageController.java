package top.bettercode.simpleframework.data;

import top.bettercode.simpleframework.web.BaseController;
import java.util.List;
import org.springframework.http.ResponseEntity;

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
    return ok(BaseServiceImpl.page(list));
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
