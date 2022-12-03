package top.bettercode.summer.security;

import java.util.Collections;
import java.util.List;

/**
 * @author Peter Wu
 */
public interface IResourceService {

  /**
   * @return 所有资源
   */
  default List<? extends IResource> findAllResources() {
    return Collections.emptyList();
  }

  default boolean supportsAnonymous() {
    return true;
  }
}
