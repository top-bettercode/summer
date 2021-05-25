package cn.bestwu.simpleframework.web;

import cn.bestwu.logging.annotation.NoRequestLogging;
import cn.bestwu.simpleframework.exception.ResourceNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Peter Wu
 */
@NoRequestLogging
@Controller
public class NavController {

  private static final String staticLocations = "classpath:/META-INF/resources";
  private final WebEndpointProperties webEndpointProperties;
  private final ResourceLoader resourceLoader;

  public NavController(
      WebEndpointProperties webEndpointProperties,
      ResourceLoader resourceLoader) {
    this.webEndpointProperties = webEndpointProperties;
    this.resourceLoader = resourceLoader;
  }

  @GetMapping("/doc/{path}")
  public String doc(@PathVariable("path") String path) {
    return "redirect:" + webEndpointProperties.getBasePath() + "/doc/" + path;
  }

  @GetMapping("/actuator/doc")
  public String actuatorDoc() throws IOException {
    String name = staticLocations + webEndpointProperties.getBasePath() + "/doc/";
    Resource resource = resourceLoader.getResource(name);
    if (resource.exists()) {
      File dic = resource.getFile();
      if (dic.exists()) {
        File file = new File(dic, "v1.0.html");
        if (file.exists()) {
          return "redirect:" + webEndpointProperties.getBasePath() + "/doc/v1.0.html";
        } else {
          Optional<File> first = Arrays.stream(dic.listFiles())
              .filter(f -> f.isFile() && f.getName().endsWith(".html"))
              .min(Comparator.comparing(File::getName));
          if (first.isPresent()) {
            return "redirect:" + webEndpointProperties.getBasePath() + "/doc/" + first.get()
                .getName();
          }
        }
      }
    }
    throw new ResourceNotFoundException();
  }
}
