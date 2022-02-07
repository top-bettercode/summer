package top.bettercode.simpleframework.web.form;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.lang.util.Sha512DigestUtils;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.logging.trace.TraceHttpServletRequestWrapper;
import top.bettercode.logging.trace.TraceServletInputStream;
import top.bettercode.simpleframework.AnnotatedUtils;
import top.bettercode.simpleframework.exception.BusinessException;
import top.bettercode.simpleframework.servlet.NotErrorHandlerInterceptor;
import top.bettercode.simpleframework.UserInfoHelper;

/**
 * 表单重复检
 *
 * @author Peter Wu
 */
public class FormDuplicateCheckInterceptor implements NotErrorHandlerInterceptor {

  private final Logger log = LoggerFactory.getLogger(FormDuplicateCheckInterceptor.class);
  private final IFormkeyService formkeyService;

  public FormDuplicateCheckInterceptor(IFormkeyService formkeyService) {
    this.formkeyService = formkeyService;
  }


  @Override
  public boolean preHandlerMethod(HttpServletRequest request, HttpServletResponse response,
      HandlerMethod handler) {
    String method = request.getMethod();
    String formkey = request.getHeader("formkey");
    if (("POST".equals(method) || "PUT".equals(method))) {
      boolean hasFormKey = StringUtils.hasText(formkey);
      if (hasFormKey || AnnotatedUtils.hasAnnotation(handler, FormDuplicateCheck.class)) {
        if (!hasFormKey) {
          ServletServerHttpRequest servletServerHttpRequest = new ServletServerHttpRequest(request);
          HttpHeaders httpHeaders = servletServerHttpRequest.getHeaders();
          String headers = StringUtil.valueOf(httpHeaders);
          String params = StringUtil.valueOf(request.getParameterMap());
          formkey = headers;
          if (isFormPost(request)) {
            formkey += "::" + params;
          } else if (request instanceof TraceHttpServletRequestWrapper) {
            try {
              InputStream body = servletServerHttpRequest.getBody();
              if (body instanceof TraceServletInputStream) {
                formkey += "::" + StreamUtils.copyToString(body, Charset.defaultCharset());
                body.reset();
              } else {
                return true;
              }
            } catch (IOException ignored) {
              return true;
            }
          } else {
            return true;
          }
        }
        Object userInfo = UserInfoHelper.get(request);
        String userKey = StringUtil.valueOf(userInfo);

        String servletPath = request.getServletPath();

        formkey = Sha512DigestUtils.shaHex(userKey + servletPath + formkey);

        if (formkeyService.exist(formkey)) {
          throw new BusinessException(String.valueOf(HttpStatus.BAD_GATEWAY.value()),
              "请勿重复提交");
        }
        formkeyService.putKey(formkey);
      }
    }
    return true;
  }

  private static boolean isFormPost(HttpServletRequest request) {
    String contentType = request.getContentType();
    return contentType != null && contentType.contains("application/x-www-form-urlencoded")
        && HttpMethod.POST.matches(request.getMethod());
  }


}
