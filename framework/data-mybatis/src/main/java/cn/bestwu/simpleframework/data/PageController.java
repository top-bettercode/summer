package cn.bestwu.simpleframework.data;

import cn.bestwu.simpleframework.web.BaseController;
import java.util.List;
import org.springframework.http.ResponseEntity;

/**
 * @author Peter Wu
 */
public class PageController extends BaseController {

  /**
   * @param <T> T
   * @param list list
   * @return 200 ResponseEntity
   */
  protected <T> ResponseEntity page(List<T> list) {
    return ok(BaseServiceImpl.page(list));
  }
}
