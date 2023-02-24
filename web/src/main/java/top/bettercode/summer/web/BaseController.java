package top.bettercode.summer.web;

import java.util.Locale;
import java.util.function.Supplier;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import top.bettercode.summer.tools.lang.util.ParameterUtil;
import top.bettercode.summer.web.error.ErrorAttributes;
import top.bettercode.summer.web.exception.ResourceNotFoundException;
import top.bettercode.summer.web.support.DeviceUtil;

/**
 * 基础Controller
 *
 * @author Peter Wu
 */
@ConditionalOnWebApplication
public class BaseController extends Response {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired(required = false)
  protected HttpServletRequest request;
  @Autowired(required = false)
  protected HttpServletResponse response;
  @Autowired
  private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;
  @Autowired(required = false)
  private ServletContext servletContext;
  @Autowired
  private MessageSource messageSource;

  public void plainTextError() {
    request.setAttribute(ErrorAttributes.IS_PLAIN_TEXT_ERROR, true);
  }

  /**
   * 得到国际化信息 未找到时返回代码 code
   *
   * @param code 模板
   * @param args 参数
   * @return 信息
   */
  public String getText(Object code, Object... args) {
    String codeString = String.valueOf(code);
    return messageSource.getMessage(codeString, args, codeString,
        request == null ? Locale.CHINA : request.getLocale());
  }

  /**
   * 得到国际化信息，未找到时返回 {@code null}
   *
   * @param code 模板
   * @param args 参数
   * @return 信息
   */
  public String getTextDefaultNull(Object code, Object... args) {
    return messageSource.getMessage(String.valueOf(code), args, null,
        request == null ? Locale.CHINA : request.getLocale());
  }

  /**
   * @param path 路径
   * @return 真实路径
   */
  public String getRealPath(String path) {
    return servletContext.getRealPath(path);
  }

  /**
   * @return UserAgent
   */
  public String getUserAgent() {
    return DeviceUtil.getUserAgent(request);
  }

  /**
   * @param key 参数名称
   * @return 是否存在此参数（非空），此方法在request body方式提交数据时可能无效
   */
  protected boolean hasParameter(String key) {
    return ParameterUtil.hasParameter(request.getParameterMap(), key);
  }

  /**
   * @param key 参数名称
   * @return 是否存在此参数（可为空）
   */
  protected boolean hasParameterKey(String key) {
    return ParameterUtil.hasParameterKey(request.getParameterMap(), key);
  }

  protected void hasText(String param, String paramName) {
    if (!StringUtils.hasText(param)) {
      throw new IllegalArgumentException(getText("param.notnull", paramName));
    }
  }

  protected void notNull(Object param, String paramName) {
    if (param == null) {
      throw new IllegalArgumentException(getText("param.notnull", paramName));
    }
  }

  protected void assertOk(RespEntity<?> respEntity) {
    RespEntity.assertOk(respEntity);
  }

  protected void assertOk(RespEntity<?> respEntity, String message) {
    RespEntity.assertOk(respEntity, message);
  }

  public static Supplier<? extends RuntimeException> notFound() {
    return ResourceNotFoundException::new;
  }

  public static Supplier<? extends RuntimeException> notFound(String msg) {
    return () -> new ResourceNotFoundException(msg);
  }

}
