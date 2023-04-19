package top.bettercode.summer.web.form;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.lang.operation.RequestConverter;
import top.bettercode.summer.tools.lang.trace.TraceHttpServletRequestWrapper;
import top.bettercode.summer.tools.lang.util.Sha512DigestUtils;
import top.bettercode.summer.tools.lang.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Wu
 */
public interface IFormkeyService {

    Logger log = LoggerFactory.getLogger(IFormkeyService.class);

    default boolean checkRequest(HttpServletRequest request, String formKeyName, boolean autoFormKey, long expireSeconds, String message) {
        return checkRequest(request, formKeyName, autoFormKey, expireSeconds, message, null, null);
    }

    default boolean checkRequest(HttpServletRequest request, String formKeyName, boolean autoFormKey, long expireSeconds, String message, String[] ignoreHeaders, String[] ignoreParams) {
        String formkey = getFormkey(request, formKeyName, autoFormKey);
        return checkRequest(request, formkey, expireSeconds, message);
    }

    default boolean checkRequest(HttpServletRequest request, String formkey, long expireSeconds, String message) {
        if (formkey == null) {
            return true;
        } else if (exist(formkey, expireSeconds)) {
            throw new FormDuplicateException(message);
        } else {
            request.setAttribute(FormDuplicateCheckInterceptor.FORM_KEY, formkey);
            return true;
        }
    }

    default void cleanKey(HttpServletRequest request) {
        String formkey = (String) request.getAttribute(FormDuplicateCheckInterceptor.FORM_KEY);
        if (formkey != null) {
            remove(formkey);
            if (log.isTraceEnabled()) {
                log.trace("{} remove:{}", request.getRequestURI(), formkey);
            }
        }

    }

    @Nullable
    default String getFormkey(HttpServletRequest request, String formKeyName, boolean autoFormKey) {
        return getFormkey(request, formKeyName, autoFormKey, null, null);
    }

    @Nullable
    default String getFormkey(HttpServletRequest request, String formKeyName, boolean autoFormKey, String[] ignoreHeaders, String[] ignoreParams) {
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
                MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<>(servletServerHttpRequest.getHeaders());
                if (ignoreHeaders != null) {
                    for (String ignoreHeader : ignoreHeaders) {
                        httpHeaders.remove(ignoreHeader);
                    }
                }
                formkey = StringUtil.valueOf(httpHeaders);
                Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());

                if (ignoreParams != null) {
                    for (String ignoreParam : ignoreParams) {
                        parameterMap.remove(ignoreParam);
                    }
                }

                String params = StringUtil.valueOf(parameterMap);
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
