package top.bettercode.simpleframework.data;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Peter Wu
 */
public class BaseSimpleService<M> {

  @Autowired
  protected M baseMapper;

  public M getRepository() {
    return baseMapper;
  }

}
