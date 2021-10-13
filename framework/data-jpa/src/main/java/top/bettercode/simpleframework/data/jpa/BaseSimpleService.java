package top.bettercode.simpleframework.data.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Wu
 */
public class BaseSimpleService<M> {

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final M repository;

  public BaseSimpleService(M repository) {
    this.repository = repository;
  }

  public M getRepository() {
    return repository;
  }

}
