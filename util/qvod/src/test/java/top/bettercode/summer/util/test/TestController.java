package top.bettercode.summer.util.test;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.bettercode.simpleframework.web.BaseController;
import top.bettercode.simpleframework.web.form.FormDuplicateCheck;

/**
 * @author Peter Wu
 */
@RestController
@Validated
public class TestController extends BaseController {

  @FormDuplicateCheck
  @RequestMapping(value = "/test")
  public Object test() {
    return ok(new DataBean());
  }

}