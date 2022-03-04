package top.bettercode.simpleframework.web.error;

import org.springframework.web.context.request.RequestAttributes;
import top.bettercode.simpleframework.web.IRespEntity;
import top.bettercode.simpleframework.web.RespEntity;

/**
 * @author Peter Wu
 */
public interface IErrorRespEntityHandler {

  IRespEntity handle(RequestAttributes requestAttributes, RespEntity<Object> respEntity);

}
