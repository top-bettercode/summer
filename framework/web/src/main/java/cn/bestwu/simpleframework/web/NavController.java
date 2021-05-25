package cn.bestwu.simpleframework.web;

import cn.bestwu.logging.annotation.NoRequestLogging;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Peter Wu
 */
@NoRequestLogging
@Controller
public class NavController {

  private final WebEndpointProperties webEndpointProperties;

  public NavController(
      WebEndpointProperties webEndpointProperties) {
    this.webEndpointProperties = webEndpointProperties;
  }

  @GetMapping("/doc/{path}")
  public String doc(@PathVariable("path") String path) {
    return "redirect:" + webEndpointProperties.getBasePath() + "/doc/" + path;
  }

}
