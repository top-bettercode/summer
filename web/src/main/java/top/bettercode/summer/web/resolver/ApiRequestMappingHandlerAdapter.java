package top.bettercode.summer.web.resolver;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import top.bettercode.summer.web.config.SummerWebProperties;
import top.bettercode.summer.web.error.ErrorAttributes;

/**
 * @author Peter Wu
 */
public class ApiRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

  private final SummerWebProperties summerWebProperties;
  private final ErrorAttributes errorAttributes;

  public ApiRequestMappingHandlerAdapter(SummerWebProperties summerWebProperties,
      ErrorAttributes errorAttributes) {
    this.summerWebProperties = summerWebProperties;
    this.errorAttributes = errorAttributes;
  }


  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();

    // Retrieve actual handlers to use as delegate
    HandlerMethodReturnValueHandlerComposite oldHandlers = new HandlerMethodReturnValueHandlerComposite()
        .addHandlers(this.getReturnValueHandlers());

    // Set up ResourceProcessingHandlerMethodResolver to delegate to originally configured ones
    List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>();
    newHandlers
        .add(new ApiHandlerMethodReturnValueHandler(oldHandlers, summerWebProperties,
            errorAttributes));

    // Configure the new handler to be used
    this.setReturnValueHandlers(newHandlers);
  }
}
