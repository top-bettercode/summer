package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.support.code.ICodeService;
import cn.bestwu.simpleframework.web.DataDicBean;
import cn.bestwu.simpleframework.web.serializer.annotation.JsonCode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Wu
 * @since 0.1.15
 */
public class CodeSerializerTest {

  @BeforeEach
  public void setUp() {
    CodeSerializer.setCodeService(new ICodeService() {
      @Override
      public String getName(String codeType, Serializable code) {
        return "codeName";
      }

      @Override
      public Serializable getCode(String codeType, String name) {
        return 123;
      }
    });

  }

  @Test
  public void serializeInt() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    objectMapper.addMixIn(DataDicBean.class, DataDicBean3.class);

    DataDicBean dicBean = new DataDicBean();
    dicBean.setName("张三");
    String string = objectMapper.writeValueAsString(dicBean);
    org.junit.jupiter.api.Assertions.assertEquals("{\"name\":\"张三\"}", string);
    dicBean = new DataDicBean3();
    dicBean.setName("张三");
    dicBean.setIntCode(123);
    String str2 = objectMapper.writeValueAsString(dicBean);
    org.junit.jupiter.api.Assertions.assertEquals("{\"name\":\"张三\",\"intCode\":123,\"intCodeName\":\"codeName\"}", str2);
  }

  @Test
  public void serializeString() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMin.class);

    DataDicBean dicBean = new DataDicBean();
    dicBean.setName("张三");
    String string = objectMapper.writeValueAsString(dicBean);
    org.junit.jupiter.api.Assertions.assertEquals("{\"name\":\"张三\"}", string);
    dicBean = new DataDicBean3();
    dicBean.setName("张三");
    dicBean.setCode("123");
    String str2 = objectMapper.writeValueAsString(dicBean);
    org.junit.jupiter.api.Assertions.assertEquals("{\"name\":\"张三\",\"code\":\"123\",\"codeName\":\"codeName\"}", str2);
  }

  interface DataDicBeanMin {

    @JsonCode("abc")
    String getCode();
  }

  static class DataDicBean3 extends DataDicBean2 {

    @JsonCode("abc")
    @Override
    public Integer getIntCode() {
      return super.getIntCode();
    }
  }

  static class DataDicBean2 extends DataDicBean {

    @Override
    public Integer getIntCode() {
      return super.getIntCode();
    }
  }


}