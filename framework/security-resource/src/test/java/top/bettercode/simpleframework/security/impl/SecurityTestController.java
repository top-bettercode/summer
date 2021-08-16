package top.bettercode.simpleframework.security.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.bettercode.simpleframework.security.resource.Anonymous;
import top.bettercode.simpleframework.security.resource.AuthenticationHelper;
import top.bettercode.simpleframework.security.resource.ClientAuthorize;
import top.bettercode.simpleframework.security.resource.ConfigAuthority;
import top.bettercode.simpleframework.web.BaseController;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@RestController
@ConditionalOnWebApplication
public class SecurityTestController extends BaseController {

  @RequestMapping(value = "/test")
  public Object test() {
    System.err.println("-----------------------");
    return ok("success");
  }

  @ConfigAuthority("a")
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

  @ClientAuthorize
  @RequestMapping(value = "/testClientAuth")
  public Object testClientAuth() {
    System.err.println("-----------------------");
    return ok("success");
  }

}
