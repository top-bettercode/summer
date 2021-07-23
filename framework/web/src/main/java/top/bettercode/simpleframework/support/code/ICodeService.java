package top.bettercode.simpleframework.support.code;


import java.io.Serializable;

public interface ICodeService {

  default String getName(String codeType) {
    return codeType;
  }

  default String getName(String codeType, Serializable code) {
    return (code == null) ? null : String.valueOf(code);
  }

  default Serializable getCode(String codeType, String name) {
    return null;
  }

  default DicCodes getDicCodes(String codeType) {
    return null;
  }
}
