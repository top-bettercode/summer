package cn.bestwu.simpleframework.support.code;

import java.io.Serializable;

/**
 * 数据编码
 *
 * @author Peter Wu
 */
public interface ICodeTypes extends Serializable {

  /**
   * 根据编码类型查询对应名称
   *
   * @param codeType 编码类型
   * @return 对应名称
   */
  String nameOf(String codeType);

  /**
   * 根据编码类型查询对应码表
   *
   * @param codeType 编码类型
   * @return 码表
   */
  ICodes codesOf(String codeType);

}
