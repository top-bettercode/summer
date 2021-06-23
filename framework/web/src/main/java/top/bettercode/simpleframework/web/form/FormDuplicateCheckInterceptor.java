package top.bettercode.simpleframework.web.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import top.bettercode.lang.util.Sha512DigestUtils;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.web.UserInfoHelper;

/**
 * 表单重复检
 *
 * @author Peter Wu
 */
public class FormDuplicateCheckInterceptor implements AsyncHandlerInterceptor {

  private final Logger log = LoggerFactory.getLogger(FormDuplicateCheckInterceptor.class);
  private final IFormKeyService formKeyService;

  public FormDuplicateCheckInterceptor(IFormKeyService formKeyService) {
    this.formKeyService = formKeyService;
  }


  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    String method = request.getMethod();
    if (("POST".equals(method) || "PUT".equals(method)) && handler instanceof HandlerMethod
        && ((HandlerMethod) handler).hasMethodAnnotation(FormDuplicateCheck.class)) {
      String formKey = request.getHeader("formKey");
      if (!StringUtils.hasText(formKey)) {
        HttpHeaders httpHeaders = new ServletServerHttpRequest(request).getHeaders();
        if (httpHeaders.getContentType().includes(MediaType.APPLICATION_FORM_URLENCODED)
            || httpHeaders.getContentLength() == 0) {
          String headers = StringUtil.valueOf(httpHeaders);
          String params = StringUtil.valueOf(request.getParameterMap());
          formKey = headers + "::" + params;
        } else {//其他ContentType如：application/json等不自动生成formKey。如需重复提交检查，须前端传递formKey
          return true;
        }
      }

      Object userInfo = UserInfoHelper.get(request);
      String userKey = StringUtil.valueOf(userInfo);

      String servletPath = request.getServletPath();

      formKey = Sha512DigestUtils.shaHex(userKey + servletPath + formKey);

      if (formKeyService.exist(formKey)) {
        throw new IllegalArgumentException("请勿重复提交");
      }
      formKeyService.putKey(formKey);
    }
    return true;
  }

}
