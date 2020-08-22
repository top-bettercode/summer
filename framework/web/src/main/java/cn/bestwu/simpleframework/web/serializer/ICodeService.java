package cn.bestwu.simpleframework.web.serializer;


import java.io.Serializable;

public interface ICodeService {

  default String getName(String codeType, Serializable code) {
    return (code == null) ? null : String.valueOf(code);
  }

  default Serializable getCode(String codeType, String name) {
    return null;
  }
}
