package top.bettercode.simpleframework.web.error;

import top.bettercode.simpleframework.web.IRespEntity;
import top.bettercode.simpleframework.web.RespEntity;
import org.springframework.web.context.request.RequestAttributes;

/**
 * @author Peter Wu
 */
public interface IErrorRespEntityHandler {

  IRespEntity handle(RequestAttributes requestAttributes, RespEntity<Object> respEntity);

}
