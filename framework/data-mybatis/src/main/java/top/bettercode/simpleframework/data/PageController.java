package top.bettercode.simpleframework.data;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import top.bettercode.simpleframework.web.BaseController;

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
    return ok(of(list));
  }

  @NotNull
  protected <T> PageExtra<T> of(List<T> list) {
    return BaseServiceImpl.page(list);
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
