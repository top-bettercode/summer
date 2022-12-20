package top.bettercode.summer.web.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.summer.web.AnnotatedUtils;
import top.bettercode.summer.web.servlet.NotErrorHandlerInterceptor;

/**
 * 表单重复检
 *
 * @author Peter Wu
 */
public class FormDuplicateCheckInterceptor implements NotErrorHandlerInterceptor {

  public static final String FORM_KEY = FormDuplicateCheckInterceptor.class.getName() + ".form_key";
  private final Logger log = LoggerFactory.getLogger(FormDuplicateCheckInterceptor.class);
  private final IFormkeyService formkeyService;
  private final String formKeyName;
  public final static String DEFAULT_MESSAGE = "您提交的太快了，请稍候再试。";

  public FormDuplicateCheckInterceptor(IFormkeyService formkeyService, String formKeyName) {
    this.formkeyService = formkeyService;
    this.formKeyName = formKeyName;
  }


  @Override
  public boolean preHandlerMethod(HttpServletRequest request, HttpServletResponse response,
      HandlerMethod handler) {
    FormDuplicateCheck annotation = AnnotatedUtils.getAnnotation(handler, FormDuplicateCheck.class);
    String formkey = formkeyService.getFormkey(request, formKeyName, annotation != null);
    if (formkey == null) {
      return true;
    } else if (formkeyService.exist(formkey,
        annotation == null ? -1 : annotation.expireSeconds())) {
      throw new FormDuplicateException(
          annotation == null ? DEFAULT_MESSAGE : annotation.message());
    } else {
      request.setAttribute(FORM_KEY, formkey);
      return true;
    }
  }


  @Override
  public void afterCompletionMethod(HttpServletRequest request, HttpServletResponse response,
      HandlerMethod handler, Throwable ex) {
    if (ex == null) {
      ex = getError(request);
    }

    if (ex != null) {
      String formkey = (String) request.getAttribute(FORM_KEY);
      if (formkey != null) {
        formkeyService.remove(formkey);
        if (log.isTraceEnabled()) {
          log.trace("{} remove:{}", request.getRequestURI(), formkey);
        }
      }
    }
  }


}
