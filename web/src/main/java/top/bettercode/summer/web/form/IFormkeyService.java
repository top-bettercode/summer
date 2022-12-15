package top.bettercode.summer.web.form;

import javax.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.lang.operation.RequestConverter;
import top.bettercode.summer.tools.lang.trace.TraceHttpServletRequestWrapper;
import top.bettercode.summer.tools.lang.util.Sha512DigestUtils;
import top.bettercode.summer.tools.lang.util.StringUtil;

/**
 * @author Peter Wu
 */
public interface IFormkeyService {

  Logger log = LoggerFactory.getLogger(IFormkeyService.class);

  @Nullable
  default String getFormkey(HttpServletRequest request, String formKeyName, boolean autoFormKey) {
    String digestFormkey = null;
    String formkey = request.getHeader(formKeyName);
    boolean hasFormKey = StringUtils.hasText(formkey);
    if (hasFormKey || autoFormKey) {
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
        String contentType = request.getContentType();
        boolean formPost =
            contentType != null && contentType.contains("application/x-www-form-urlencoded")
                && HttpMethod.POST.matches(request.getMethod());
        if (!formPost) {
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

  boolean exist(String formkey, long expireSeconds);

  void remove(String formkey);
}
