package top.bettercode.simpleframework.web.serializer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.bettercode.simpleframework.web.DataDicBean;
import top.bettercode.simpleframework.web.serializer.annotation.JsonCode;

/**
 * @author Peter Wu
 * @since 0.1.15
 */
public class CodeSerializerTest {

  @BeforeEach
  public void setUp() {
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
    org.junit.jupiter.api.Assertions
        .assertEquals("{\"name\":\"张三\",\"intCode\":123,\"intCodeName\":\"123\"}", str2);
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
    org.junit.jupiter.api.Assertions
        .assertEquals("{\"name\":\"张三\",\"code\":\"123\",\"codeName\":\"123\"}", str2);
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