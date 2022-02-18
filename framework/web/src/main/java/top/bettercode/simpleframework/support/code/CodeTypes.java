package top.bettercode.simpleframework.support.code;

import top.bettercode.simpleframework.config.SerializerConfiguration;
import top.bettercode.simpleframework.support.ApplicationContextHolder;

/**
 * 数据编码
 *
 * @author Peter Wu
 */
public abstract class CodeTypes {

  private static ICodeService CODE_SERVICE;


  public static ICodeService getCodeService() {
    if (CODE_SERVICE == null) {
      CODE_SERVICE = ApplicationContextHolder.getBean(SerializerConfiguration.CODE_SERVICE_BEAN_NAME, ICodeService.class);
    }
    return CODE_SERVICE;
  }
}
