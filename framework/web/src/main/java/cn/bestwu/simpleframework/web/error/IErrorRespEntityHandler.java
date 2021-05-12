package cn.bestwu.simpleframework.web.error;

import cn.bestwu.simpleframework.web.IRespEntity;
import cn.bestwu.simpleframework.web.RespEntity;
import org.springframework.web.context.request.RequestAttributes;

/**
 * @author Peter Wu
 */
public interface IErrorRespEntityHandler {

  IRespEntity handle(RequestAttributes requestAttributes, RespEntity<Object> respEntity);

}
