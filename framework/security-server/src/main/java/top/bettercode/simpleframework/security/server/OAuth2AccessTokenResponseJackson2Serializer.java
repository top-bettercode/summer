package top.bettercode.simpleframework.security.server;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import top.bettercode.simpleframework.config.WebProperties;
import top.bettercode.simpleframework.web.RespEntity;

public class OAuth2AccessTokenResponseJackson2Serializer extends
    StdSerializer<OAuth2AccessTokenResponse> {

  private static final long serialVersionUID = -596973630350452304L;

  private final WebProperties webProperties;
  private final HttpServletRequest request;

  public OAuth2AccessTokenResponseJackson2Serializer(
      WebProperties webProperties, HttpServletRequest request) {
    super(OAuth2AccessTokenResponse.class);
    this.webProperties = webProperties;
    this.request = request;
  }

  @Override
  public void serialize(OAuth2AccessTokenResponse value, JsonGenerator jgen,
      SerializerProvider provider) throws IOException {
    if (webProperties.wrapEnable(request)) {
      jgen.writeObject(RespEntity.ok(value));
    } else {
      jgen.writeObject(value);
    }
  }

}
