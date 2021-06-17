package top.bettercode.simpleframework.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import top.bettercode.lang.util.ParameterUtil;
import top.bettercode.lang.util.Sha1DigestUtil;
import top.bettercode.simpleframework.support.DeviceUtil;
import top.bettercode.simpleframework.web.error.ErrorAttributes;

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

  /**
   * 支持客户端缓存
   *
   * @param object object
   * @return 200 ResponseEntity
   */
  protected ResponseEntity<?> cacheable(Object object) {
    return cacheable(object, null);
  }

  /**
   * 支持客户端缓存
   *
   * @param serializationView serializationView
   * @param object            object
   * @return 200 ResponseEntity
   */
  protected ResponseEntity<?> cacheable(Object object, Class<?> serializationView) {
    try {
      ObjectMapper objectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
      ObjectWriter objectWriter;
      if (serializationView != null) {
        objectWriter = objectMapper.writerWithView(serializationView);
      } else {
        objectWriter = objectMapper.writer();
      }
      String body = objectWriter.writeValueAsString(object);
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType(MediaType.APPLICATION_JSON);
      httpHeaders.setETag("\"".concat(Sha1DigestUtil.shaHex(body)).concat("\""));
      return ResponseEntity.ok().headers(cacheControl(httpHeaders)).body(body);
    } catch (JsonProcessingException e) {
      return ok(object);
    }
  }

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

}
