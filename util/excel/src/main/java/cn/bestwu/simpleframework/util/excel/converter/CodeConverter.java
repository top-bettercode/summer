package cn.bestwu.simpleframework.util.excel.converter;

import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
import java.io.Serializable;

/**
 * @author Peter Wu
 */
public class CodeConverter extends AbstractCodeConverter {

  private static final CodeConverter instance = new CodeConverter();

  private CodeConverter() {
    super(true);
  }

  public static CodeConverter newInstance() {
    return instance;
  }

  @Override
  public String getName(String codeType, Serializable code) {
    return CodeSerializer.getName(codeType, code);
  }

  @Override
  protected Object getCode(String codeType, String name) {
    return CodeSerializer.getCode(codeType, name);
  }
}
