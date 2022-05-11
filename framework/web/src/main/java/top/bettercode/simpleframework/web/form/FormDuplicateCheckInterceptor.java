package top.bettercode.simpleframework.web.form;

import java.io.InputStream;
import java.nio.charset.Charset;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.Nullable;
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

/**
 * 表单重复检
 *
 * @author Peter Wu
 */
public class FormDuplicateCheckInterceptor implements NotErrorHandlerInterceptor {

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
    String method = request.getMethod();
    String formkey = request.getHeader(formKeyName);
    if (("POST".equals(method) || "PUT".equals(method))) {
      boolean hasFormKey = StringUtils.hasText(formkey);
      if (hasFormKey || AnnotatedUtils.hasAnnotation(handler, FormDuplicateCheck.class)) {
        if (log.isDebugEnabled()) {
          log.debug(request.getServletPath() + " formDuplicateCheck");
        }
        String digestFormkey = getFormkey(request, method, formkey, hasFormKey);
        if (digestFormkey == null) {
          return true;
        }
        if (formkeyService.exist(digestFormkey)) {
          throw new BusinessException(String.valueOf(HttpStatus.BAD_GATEWAY.value()),
              "请勿重复提交");
        }
      }
    }
    return true;
  }

  @Nullable
  private String getFormkey(HttpServletRequest request, String method, String formkey,
      boolean hasFormKey) {
    if (!hasFormKey) {
      ServletServerHttpRequest servletServerHttpRequest = new ServletServerHttpRequest(
          request);
      HttpHeaders httpHeaders = servletServerHttpRequest.getHeaders();
      formkey = StringUtil.valueOf(httpHeaders);
      String params = StringUtil.valueOf(request.getParameterMap());
      formkey += "::" + params;
      if (!isFormPost(request)) {
        TraceHttpServletRequestWrapper traceHttpServletRequestWrapper = getTraceHttpServletRequestWrapper(
            request);
        if (traceHttpServletRequestWrapper != null) {
          try {
            InputStream body = traceHttpServletRequestWrapper.getInputStream();
            if (body instanceof TraceServletInputStream) {
              formkey += "::" + StreamUtils.copyToString(body, Charset.defaultCharset());
              body.reset();
            } else {
              log.info(request.getServletPath()
                  + " not traceServletInputStream ignore formDuplicateCheck");
              return null;
            }
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

    String servletPath = request.getServletPath();
    formkey = formkey + method + servletPath;
    String digestFormkey = Sha512DigestUtils.shaHex(formkey + method + servletPath);
    if (log.isDebugEnabled()) {
      log.debug("{} formkey:{},digestFormkey:{}", request.getServletPath(), formkey,
          digestFormkey);
    }
    return digestFormkey;
  }

  private TraceHttpServletRequestWrapper getTraceHttpServletRequestWrapper(ServletRequest request) {
    if (request instanceof TraceHttpServletRequestWrapper) {
      return (TraceHttpServletRequestWrapper) request;
    } else if (request instanceof HttpServletRequestWrapper) {
      return getTraceHttpServletRequestWrapper(((HttpServletRequestWrapper) request).getRequest());
    } else {
      return null;
    }
  }

  private static boolean isFormPost(HttpServletRequest request) {
    String contentType = request.getContentType();
    return contentType != null && contentType.contains("application/x-www-form-urlencoded")
        && HttpMethod.POST.matches(request.getMethod());
  }


}
