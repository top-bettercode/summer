package cn.bestwu.simpleframework;


import cn.bestwu.simpleframework.web.BaseController;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Peter Wu
 */
@SpringBootApplication
@RestController
public class DataTestController extends BaseController {

  @GetMapping(value = "/test")
  public Object test() {
    return ok("sdf");
  }

}