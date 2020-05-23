package cn.bestwu.simpleframework.web.error;

import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.logging.RequestLoggingHandler;
import cn.bestwu.logging.operation.Operation;
import cn.bestwu.logging.operation.OperationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

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
      response.setStackTrace("");
      Map<String, Object> errorAttributes = this.errorAttributes
          .getErrorAttributes(webRequest, false);
      if (response.getContent().length == 0) {
        try {
          response.setContent(
              StringUtil.getINDENT_OUTPUT_OBJECT_MAPPER().writeValueAsBytes(errorAttributes));
        } catch (JsonProcessingException e) {
          log.error(e.getMessage(), e);
        }
      } else {
        log.error("异常:{}", StringUtil.valueOf(errorAttributes, true));
      }
    }
  }
}
