package cn.bestwu.simpleframework.security.exception;

import cn.bestwu.simpleframework.web.IRespEntity;
import cn.bestwu.simpleframework.web.RespEntity;
import cn.bestwu.simpleframework.web.error.IErrorRespEntityHandler;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 自定义异常Jackson2序列化
 *
 * @author Peter Wu
 */
@SuppressWarnings("deprecation")
public class OAuth2ExceptionJackson2Serializer extends StdSerializer<OAuth2Exception> {

  private static final long serialVersionUID = 5223328500862406031L;
  private static IErrorRespEntityHandler ERROR_RESP_ENTITY_HANDLER;

  public OAuth2ExceptionJackson2Serializer() {
    super(OAuth2Exception.class);
  }

  public static void setErrorRespEntityHandler(
      IErrorRespEntityHandler errorRespEntityHandler) {
    ERROR_RESP_ENTITY_HANDLER = errorRespEntityHandler;
  }

  @Override
  public void serialize(OAuth2Exception value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
    RespEntity<Object> respEntity = new RespEntity<>(String.valueOf(value.getHttpErrorCode()),
        value.getMessage());

    Throwable cause = value.getCause();
    Map<String, String> additionalInformation = value.getAdditionalInformation();
    if (cause instanceof IllegalUserException) {
      Map<String, String> errors = ((IllegalUserException) cause).getErrors();
      if (!CollectionUtils.isEmpty(errors)) {
        if (additionalInformation == null) {
          additionalInformation = new HashMap<>();
        }
        additionalInformation.putAll(errors);
      }
    }
    if (additionalInformation != null) {
      respEntity.setErrors(additionalInformation);
    }
    if (ERROR_RESP_ENTITY_HANDLER != null) {
      ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
          .getRequestAttributes();
      IRespEntity respEntity1 = ERROR_RESP_ENTITY_HANDLER.handle(requestAttributes, respEntity);
      jgen.writeObject(respEntity1);
    } else {
      jgen.writeObject(respEntity);
    }
  }

}