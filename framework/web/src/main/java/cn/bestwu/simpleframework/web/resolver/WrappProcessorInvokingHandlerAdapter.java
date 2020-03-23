package cn.bestwu.simpleframework.web.resolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

public class WrappProcessorInvokingHandlerAdapter extends RequestMappingHandlerAdapter {

  private static final Method RETURN_VALUE_HANDLER_METHOD = ReflectionUtils
      .findMethod(WrappProcessorInvokingHandlerAdapter.class, "getReturnValueHandlers");

  private final Boolean okEnable;
  private final Boolean wrapEnable;


  public WrappProcessorInvokingHandlerAdapter(Boolean okEnable, Boolean wrapEnable) {
    this.okEnable = okEnable;
    this.wrapEnable = wrapEnable;
  }


  @Override
  public void afterPropertiesSet() {

    super.afterPropertiesSet();

    // Retrieve actual handlers to use as delegate
    HandlerMethodReturnValueHandlerComposite oldHandlers = getReturnValueHandlersComposite();

    // Set up ResourceProcessingHandlerMethodResolver to delegate to originally configured ones
    List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>();
    newHandlers.add(new WrappHandlerMethodReturnValueHandler(oldHandlers, okEnable, wrapEnable));

    // Configure the new handler to be used
    this.setReturnValueHandlers(newHandlers);
  }

  /**
   * Gets a {@link HandlerMethodReturnValueHandlerComposite} for return handlers, dealing with API
   * changes introduced in Spring 4.0.
   *
   * @return a HandlerMethodReturnValueHandlerComposite
   */
  @SuppressWarnings("unchecked")
  private HandlerMethodReturnValueHandlerComposite getReturnValueHandlersComposite() {

    Object handlers = ReflectionUtils.invokeMethod(RETURN_VALUE_HANDLER_METHOD, this);

    if (handlers instanceof HandlerMethodReturnValueHandlerComposite) {
      return (HandlerMethodReturnValueHandlerComposite) handlers;
    }

    return new HandlerMethodReturnValueHandlerComposite()
        .addHandlers((List<? extends HandlerMethodReturnValueHandler>) handlers);
  }
}
