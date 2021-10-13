package top.bettercode.simpleframework.data.jpa;

/**
 * @author Peter Wu
 */
public class BaseServiceImpl<T, ID, M extends BaseRepository<T, ID>> extends BaseService<T, ID, M> {

  public BaseServiceImpl(M repository) {
    super(repository);
  }

}
