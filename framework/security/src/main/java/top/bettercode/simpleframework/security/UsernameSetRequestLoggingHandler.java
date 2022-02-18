package top.bettercode.simpleframework.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.method.HandlerMethod;
import top.bettercode.logging.RequestLoggingHandler;
import top.bettercode.logging.operation.Operation;

/**
 * @author Peter Wu
 */
public class UsernameSetRequestLoggingHandler implements RequestLoggingHandler {

  @Override
  public void handle(@NotNull Operation operation, @Nullable HandlerMethod handler) {
    operation.getRequest().setUsername(AuthenticationHelper.getUsername().orElse("Anonymous"));
  }
}
