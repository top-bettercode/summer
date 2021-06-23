package top.bettercode.simpleframework.web.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import top.bettercode.lang.util.Sha512DigestUtils;

/**
 * 表单重复检查,须检查的接口包括：入库、出库、转移、注册、注销
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
      String requestURL = request.getRequestURL().toString();
      String formKey = request.getHeader("formKey");
      if (!StringUtils.hasText(formKey)) {
        return true;
      }

      formKey = Sha512DigestUtils.shaHex(requestURL + formKey);

      if (formKeyService.exist(formKey)) {
        throw new IllegalArgumentException("请勿重复提交");
      }
      formKeyService.putKey(formKey);
    }
    return true;
  }

}
