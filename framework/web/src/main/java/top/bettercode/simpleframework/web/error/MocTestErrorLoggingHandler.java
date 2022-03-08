package top.bettercode.simpleframework.web.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.logging.RequestLoggingHandler;
import top.bettercode.logging.operation.Operation;
import top.bettercode.logging.operation.OperationResponse;
import top.bettercode.simpleframework.web.RespEntity;

/**
 * @author Peter Wu
 */
public class MocTestErrorLoggingHandler implements RequestLoggingHandler {

  private final Logger log = LoggerFactory.getLogger(MocTestErrorLoggingHandler.class);
  private final ErrorAttributes errorAttributes;

  private final WebRequest webRequest;

  public MocTestErrorLoggingHandler(
      ErrorAttributes errorAttributes,
      WebRequest webRequest) {
    this.errorAttributes = errorAttributes;
    this.webRequest = webRequest;
  }

  @Override
  public void handle(@NotNull Operation operation, @Nullable HandlerMethod handler) {
    OperationResponse response = operation.getResponse();
    String stackTrace = response.getStackTrace();
    if (StringUtils.hasText(stackTrace)) {
      Map<String, Object> errorAttributes = this.errorAttributes
          .getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
      if (response.getContent().length == 0) {
        try {
          Boolean isPlainText = (Boolean) webRequest
              .getAttribute(ErrorAttributes.IS_PLAIN_TEXT_ERROR,
                  RequestAttributes.SCOPE_REQUEST);
          if (isPlainText != null && isPlainText) {
            String msg = (String) errorAttributes.get(RespEntity.KEY_MESSAGE);
            if (msg == null) {
              msg = "";
            }
            response.setContent(msg.getBytes(
                StandardCharsets.UTF_8));
          } else {
            response.setContent(
                StringUtil.getINDENT_OUTPUT_OBJECT_MAPPER().writeValueAsBytes(errorAttributes));
          }

        } catch (JsonProcessingException e) {
          log.error(e.getMessage(), e);
        }
      } else {
        log.error("异常:{}", StringUtil.valueOf(errorAttributes, true));
      }
      response.setStackTrace("");
    }
  }
}
