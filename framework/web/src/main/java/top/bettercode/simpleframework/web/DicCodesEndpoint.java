package top.bettercode.simpleframework.web;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import top.bettercode.simpleframework.support.code.DicCodes;
import top.bettercode.simpleframework.support.code.ICodeService;

/**
 * @author Peter Wu
 */
@Endpoint(id = "code")
public class DicCodesEndpoint {

  private final ICodeService codeService;

  public DicCodesEndpoint(ICodeService codeService) {
    this.codeService = codeService;
  }

  @WriteOperation
  public Object write(String codeTypeKey, String name) {
    codeService.put(codeTypeKey, name);
    return codeService.getDicCodes(codeTypeKey.substring(0, codeTypeKey.indexOf('.')));
  }


  @ReadOperation
  public DicCodes environmentEntry(@Selector String codeType) {
    return codeService.getDicCodes(codeType);
  }

}
