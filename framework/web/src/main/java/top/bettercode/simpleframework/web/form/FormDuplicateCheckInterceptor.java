package top.bettercode.simpleframework.web.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import top.bettercode.lang.util.Sha512DigestUtils;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.exception.BusinessException;
import top.bettercode.simpleframework.web.UserInfoHelper;

/**
 * 表单重复检
 *
 * @author Peter Wu
 */
public class FormDuplicateCheckInterceptor implements AsyncHandlerInterceptor {

  private final Logger log = LoggerFactory.getLogger(FormDuplicateCheckInterceptor.class);
  private final IFormkeyService formkeyService;

  public FormDuplicateCheckInterceptor(IFormkeyService formkeyService) {
    this.formkeyService = formkeyService;
  }


  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    String method = request.getMethod();
    String formkey = request.getHeader("formkey");
    if (("POST".equals(method) || "PUT".equals(method)) && handler instanceof HandlerMethod) {
      boolean hasFormKey = StringUtils.hasText(formkey);
      if (hasFormKey || ((HandlerMethod) handler).hasMethodAnnotation(FormDuplicateCheck.class)) {
        if (!hasFormKey) {
          HttpHeaders httpHeaders = new ServletServerHttpRequest(request).getHeaders();
          if (httpHeaders.getContentType().includes(MediaType.APPLICATION_FORM_URLENCODED)
              || httpHeaders.getContentLength() == 0) {
            String headers = StringUtil.valueOf(httpHeaders);
            String params = StringUtil.valueOf(request.getParameterMap());
            formkey = headers + "::" + params;
          } else {//其他ContentType如：application/json等不自动生成formkey。如需重复提交检查，须前端传递formkey
            return true;
          }
        }

        Object userInfo = UserInfoHelper.get(request);
        String userKey = StringUtil.valueOf(userInfo);

        String servletPath = request.getServletPath();

        formkey = Sha512DigestUtils.shaHex(userKey + servletPath + formkey);

        if (formkeyService.exist(formkey)) {
          throw new BusinessException(String.valueOf(HttpStatus.CREATED.value()),
              "请勿重复提交");
        }
        formkeyService.putKey(formkey);
      }
    }
    return true;
  }

}
