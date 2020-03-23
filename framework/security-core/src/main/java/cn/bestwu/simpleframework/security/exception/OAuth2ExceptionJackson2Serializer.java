package cn.bestwu.simpleframework.security.exception;

import cn.bestwu.simpleframework.web.RespEntity;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.util.CollectionUtils;

/**
 * 自定义异常Jackson2序列化
 *
 * @author Peter Wu
 */
public class OAuth2ExceptionJackson2Serializer extends StdSerializer<OAuth2Exception> {

  private static final long serialVersionUID = 5223328500862406031L;

  public OAuth2ExceptionJackson2Serializer() {
    super(OAuth2Exception.class);
  }

  @Override
  public void serialize(OAuth2Exception value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
    jgen.writeStartObject();
    jgen.writeStringField(RespEntity.KEY_STATUS, String.valueOf(value.getHttpErrorCode()));
    jgen.writeStringField(RespEntity.KEY_MESSAGE, value.getMessage());
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
      jgen.writeObjectField(RespEntity.KEY_ERRORS, additionalInformation);
    }
    jgen.writeEndObject();
  }

}