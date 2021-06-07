package top.bettercode.simpleframework.support.code;

/**
 * 数据编码
 *
 * @author Peter Wu
 */
public abstract class CodeTypes  {

  private static ICodeService CODE_SERVICE;

  public static void setCodeService(ICodeService codeService) {
    CODE_SERVICE = codeService;
  }

  public static ICodeService getCodeService() {
    return CODE_SERVICE;
  }
}
