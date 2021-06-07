package top.bettercode.simpleframework.security.resource;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;

/**
 * 资源，包含权限信息
 *
 * @author Peter Wu
 */
public interface IResource extends Serializable {

  /**
   * @return 资源描述, 以 ((method(|method)*)*:url(|url)*,?)+
   */
  @NotBlank
  String getRess();

  /**
   * @return 权限属性
   */
  @NotBlank
  String getMark();
}
