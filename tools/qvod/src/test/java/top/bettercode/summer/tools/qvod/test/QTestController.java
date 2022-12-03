package top.bettercode.summer.tools.qvod.test;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.bettercode.summer.web.BaseController;
import top.bettercode.summer.web.form.FormDuplicateCheck;

/**
 * @author Peter Wu
 */
@RestController
@Validated
public class QTestController extends BaseController {

  @FormDuplicateCheck
  @RequestMapping(value = "/test")
  public Object test() {
    return ok(new DataBean());
  }

}