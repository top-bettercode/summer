package top.bettercode.simpleframework.web.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.lang.util.Sha512DigestUtils;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.logging.operation.RequestConverter;
import top.bettercode.logging.trace.TraceHttpServletRequestWrapper;
import top.bettercode.simpleframework.AnnotatedUtils;
import top.bettercode.simpleframework.servlet.NotErrorHandlerInterceptor;

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

  public FormDuplicateCheckInterceptor(IFormkeyService formkeyService, String formKeyName) {
    this.formkeyService = formkeyService;
    this.formKeyName = formKeyName;
  }


  @Override
  public boolean preHandlerMethod(HttpServletRequest request, HttpServletResponse response,
      HandlerMethod handler) {
    FormDuplicateCheck annotation = AnnotatedUtils.getAnnotation(handler, FormDuplicateCheck.class);
    String formkey = getFormkey(request, handler, annotation);
    if (formkey == null) {
      return true;
    } else if (formkeyService.exist(formkey,
        annotation == null ? -1 : annotation.expireSeconds())) {
      throw new FormDuplicateException(annotation.message());
    } else {
      request.setAttribute(FORM_KEY, formkey);
      return true;
    }
  }

  @Nullable
  private String getFormkey(HttpServletRequest request, HandlerMethod handler,
      FormDuplicateCheck annotation) {
    String digestFormkey = null;
    String formkey = request.getHeader(formKeyName);
    boolean hasFormKey = StringUtils.hasText(formkey);
    if (hasFormKey || annotation != null) {
      if (log.isTraceEnabled()) {
        log.trace(request.getServletPath() + " formDuplicateCheck");
      }
      if (!hasFormKey) {
        ServletServerHttpRequest servletServerHttpRequest = new ServletServerHttpRequest(
            request);
        HttpHeaders httpHeaders = servletServerHttpRequest.getHeaders();
        formkey = StringUtil.valueOf(httpHeaders);
        String params = StringUtil.valueOf(request.getParameterMap());
        formkey += "::" + params;
        if (!isFormPost(request)) {
          TraceHttpServletRequestWrapper traceHttpServletRequestWrapper = RequestConverter.INSTANCE.getRequestWrapper(
              request, TraceHttpServletRequestWrapper.class);
          if (traceHttpServletRequestWrapper != null) {
            try {
              formkey += "::" + traceHttpServletRequestWrapper.getContent();
            } catch (Exception e) {
              log.info(
                  request.getServletPath() + e.getMessage() + " ignore formDuplicateCheck");
              return null;
            }
          } else {
            log.info(request.getServletPath()
                + " not traceHttpServletRequestWrapper ignore formDuplicateCheck");
            return null;
          }
        }
      }

      formkey = formkey + request.getMethod() + request.getRequestURI();
      digestFormkey = Sha512DigestUtils.shaHex(formkey);
      if (log.isTraceEnabled()) {
        log.trace("{} formkey:{},digestFormkey:{}", request.getRequestURI(), formkey,
            digestFormkey);
      }
    }
    return digestFormkey;
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

  private static boolean isFormPost(HttpServletRequest request) {
    String contentType = request.getContentType();
    return contentType != null && contentType.contains("application/x-www-form-urlencoded")
        && HttpMethod.POST.matches(request.getMethod());
  }


}
