package top.bettercode.simpleframework.support.code;


import java.io.Serializable;

public interface ICodeService {

  default String getName(String codeType) {
    return codeType;
  }

  default String getName(String codeType, Serializable code) {
    if (code == null) {
      return null;
    } else {
      DicCodes dicCodes = getDicCodes(codeType);
      return dicCodes == null ? String.valueOf(code) : dicCodes.getName(code);
    }
  }

  default Serializable getCode(String codeType, String name) {
    if (name == null) {
      return null;
    } else {
      DicCodes dicCodes = getDicCodes(codeType);
      return dicCodes == null ? null : dicCodes.getCode(name);
    }
  }

  default DicCodes getDicCodes(String codeType) {
    return null;
  }
}
