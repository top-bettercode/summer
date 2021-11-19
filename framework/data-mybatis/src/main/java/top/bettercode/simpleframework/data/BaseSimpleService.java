package top.bettercode.simpleframework.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Peter Wu
 */
public class BaseSimpleService<M> {

  protected Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  protected M baseMapper;

  public M getRepository() {
    return baseMapper;
  }

}
