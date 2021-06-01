package cn.bestwu.simpleframework.web.error;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 自定义错误处理
 *
 * @author Peter Wu
 */
@ConditionalOnWebApplication
@ConditionalOnMissingBean(ErrorController.class)
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CustomErrorController extends BasicErrorController {

  private final Logger log = LoggerFactory.getLogger(CustomErrorController.class);
  private final CorsProcessor processor = new DefaultCorsProcessor();
  private final CorsConfigurationSource configSource;
  @Autowired(required = false)
  private HttpServletResponse response;

  public CustomErrorController(
      ErrorAttributes errorAttributes,
      ErrorProperties errorProperties,
      @Autowired(required = false) @Qualifier("corsConfigurationSource") CorsConfigurationSource configSource) {
    super(errorAttributes, errorProperties);
    this.configSource = configSource;
  }


  @Override
  @RequestMapping(produces = "text/html")
  public ModelAndView errorHtml(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> model = Collections.unmodifiableMap(getErrorAttributes(
        request, getErrorAttributeOptions(request, MediaType.TEXT_HTML)));
    HttpStatus status = getStatus(request);
    response.setStatus(status.value());
    ModelAndView modelAndView = resolveErrorView(request, response, status, model);
    setCors(request, response);
    return (modelAndView == null ? new ModelAndView("error", model) : modelAndView);
  }

  private void setCors(HttpServletRequest request, HttpServletResponse response) {
    if (configSource != null && CorsUtils.isCorsRequest(request)) {
      CorsConfiguration corsConfiguration = this.configSource.getCorsConfiguration(request);
      if (corsConfiguration != null) {
        try {
          this.processor.processRequest(corsConfiguration, request, response);
        } catch (IOException e) {
          log.error("跨域设置出错", e);
        }
      }
    }
  }

  @RequestMapping
  @ResponseBody
  @Override
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
    Map<String, Object> body = getErrorAttributes(request,
        getErrorAttributeOptions(request, MediaType.ALL));
    HttpStatus status = getStatus(request);
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes();
    if (requestAttributes != null) {
      setCors(request, requestAttributes.getResponse());
    }
    response.setStatus(status.value());

    return ResponseEntity.status(status).headers(noCache()).body(body);
  }

  /**
   * @return 不支持客户端缓存，不支持客户端保存数据的响应头
   */
  protected HttpHeaders noCache() {
    HttpHeaders headers = new HttpHeaders();
    headers.setCacheControl("no-cache, no-store, max-age=0, must-revalidate");
    headers.setPragma("no-cache");
    headers.setExpires(-1);
    return headers;
  }
}