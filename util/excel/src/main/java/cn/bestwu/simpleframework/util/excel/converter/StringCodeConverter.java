package cn.bestwu.simpleframework.util.excel.converter;

import cn.bestwu.simpleframework.web.serializer.CodeSerializer;
import java.io.Serializable;

/**
 * @author Peter Wu
 */
public class StringCodeConverter extends AbstractCodeConverter {

  private static final StringCodeConverter instance = new StringCodeConverter();

  private StringCodeConverter() {
    super(false);
  }

  public static StringCodeConverter newInstance() {
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
