package top.bettercode.simpleframework.data.dsl;

import top.bettercode.simpleframework.data.binding.ConditionWrapper;

/**
 * @author Peter Wu
 */
@SuppressWarnings("unchecked")
public abstract class EntityPathWrapper<E extends EntityPathWrapper<E, T>, T> extends
    ConditionWrapper<T> {

  private static final long serialVersionUID = 1L;

  public E trim(boolean trim) {
    super.setTrim(trim);
    return (E) this;
  }

  /**
   * <p>
   * 使用AND连接并换行
   * </p>
   * <p>
   *
   * @return E
   */
  @Override
  public E and() {
    return (E) super.and();
  }

  /**
   * <p>
   * 使用OR连接并换行
   * </p>
   *
   * @return E
   */
  @Override
  public E or() {
    return (E) super.or();
  }

  /**
   * <p>
   * 使用AND连接并换行
   * </p>
   * <p>
   * eg: ew.where("name='zhangsan'").and("id=11").andNew("statu=1"); 输出： WHERE
   * (name='zhangsan' AND id=11) AND (statu=1)
   * </p>
   *
   * @return this
   */
  @Override
  public E andNew() {
    return (E) super.andNew();
  }

  /**
   * <p>
   * 使用OR换行，并添加一个带()的新的条件
   * </p>
   * <p>
   * eg: ew.where("name='zhangsan'").and("id=11").orNew("statu=1"); 输出： WHERE
   * (name='zhangsan' AND id=11) OR (statu=1)
   * </p>
   *
   * @return this
   */
  @Override
  public E orNew() {
    return (E) super.orNew();
  }
}
