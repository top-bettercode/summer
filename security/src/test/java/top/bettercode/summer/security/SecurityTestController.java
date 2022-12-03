package top.bettercode.summer.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.bettercode.summer.web.BaseController;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@RestController
@ConditionalOnWebApplication
public class SecurityTestController extends BaseController {

  @RequestMapping(value = "/testDefaultAuth")
  public Object test() {
    System.err.println("-----------------------");
    return ok("success");
  }

  @CustAuth
  @EmpAuth
  @RequestMapping(value = "/testAuth")
  public Object testAuth() {
    Object authentication = AuthenticationHelper.getPrincipal();
    System.err.println("-----------------------");
    return ok("success");
  }

  @Anonymous
  @RequestMapping(value = "/testNoAuth")
  public Object testNoAuth() {
    System.err.println("-----------------------");
    return ok("success");
  }


}
