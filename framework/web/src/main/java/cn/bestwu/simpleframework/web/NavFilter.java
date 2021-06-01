package cn.bestwu.simpleframework.web;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Peter Wu
 */
public class NavFilter extends OncePerRequestFilter {

  private static final String staticLocations = "classpath:/META-INF/resources";
  private final WebEndpointProperties webEndpointProperties;
  private final ResourceLoader resourceLoader;

  public NavFilter(
      WebEndpointProperties webEndpointProperties,
      ResourceLoader resourceLoader) {
    this.webEndpointProperties = webEndpointProperties;
    this.resourceLoader = resourceLoader;
  }


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String servletPath = request.getServletPath();
    if (servletPath.startsWith("/doc")) {
      response.sendRedirect(
          request.getContextPath() + webEndpointProperties.getBasePath() + "/doc" + servletPath
              .substring(4));
      return;
    }
    if (servletPath.startsWith("/logs")) {
      response.sendRedirect(
          request.getContextPath() + webEndpointProperties.getBasePath() + "/logs" + servletPath
              .substring(5));
      return;
    }
    if ("/actuator/doc".equals(servletPath)) {
      String name = staticLocations + webEndpointProperties.getBasePath() + "/doc/";
      Resource resource = resourceLoader.getResource(name);
      if (resource.exists()) {
        File dic = resource.getFile();
        if (dic.exists()) {
          File file = new File(dic, "v1.0.html");
          if (file.exists()) {
            response.sendRedirect(
                request.getContextPath() + webEndpointProperties.getBasePath() + "/doc/v1.0.html");
            return;
          } else {
            Optional<File> first = Arrays.stream(dic.listFiles())
                .filter(f -> f.isFile() && f.getName().endsWith(".html"))
                .min(Comparator.comparing(File::getName));
            if (first.isPresent()) {
              response.sendRedirect(
                  request.getContextPath() + webEndpointProperties.getBasePath() + "/doc/" + first
                      .get()
                      .getName());
              return;
            }
          }
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
