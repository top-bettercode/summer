package cn.bestwu.simpleframework.security.impl;

import cn.bestwu.simpleframework.security.resource.Anonymous;
import cn.bestwu.simpleframework.security.resource.AuthenticationHelper;
import cn.bestwu.simpleframework.security.resource.ClientAuthorize;
import cn.bestwu.simpleframework.security.resource.ConfigAuthority;
import cn.bestwu.simpleframework.web.BaseController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Peter Wu
 * @since 1.0.0
 */
@RestController
@ConditionalOnWebApplication
@RequestMapping(name = "test")
public class SecurityTestController extends BaseController {

  @RequestMapping(value = "/test",name = "test")
  public Object test() {
    System.err.println("-----------------------");
    return ok("success");
  }

  @ConfigAuthority("a")
  @RequestMapping(value = "/testAuth",name = "testAuth")
  public Object testAuth() {
    Authentication authentication = AuthenticationHelper.getAuthentication();
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
