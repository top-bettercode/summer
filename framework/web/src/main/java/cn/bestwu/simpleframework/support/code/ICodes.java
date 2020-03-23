package cn.bestwu.simpleframework.support.code;

import java.io.Serializable;
import java.util.Map;

/**
 * 码表
 *
 * @author Peter Wu
 */
public interface ICodes extends Serializable, Map<Serializable,String> {

  /**
   * 根据标识码查询对应名称
   *
   * @param code 标识码
   * @return 对应名称
   */
  String nameOf(Serializable code);

  /**
   * 根据标识码名称查询对应标识码
   *
   * @param name 名称
   * @return 标识码
   */
  Serializable codeOf(String name);
}
