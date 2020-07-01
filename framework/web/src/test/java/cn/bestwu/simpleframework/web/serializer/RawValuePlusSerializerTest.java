package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.web.serializer.annotation.JsonRawValuePlus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

/**
 * @author Peter Wu
 * @since 0.1.15
 */
public class RawValuePlusSerializerTest {

  @Test
  public void serialize() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    String string = objectMapper.writeValueAsString(new BeanWithRaw("张三", null));
    System.err.println(string);
    string = objectMapper.writeValueAsString(new BeanWithRaw("张三", " "));
    System.err.println(string);
    string = objectMapper
        .writeValueAsString(new BeanWithRaw("张三", "{\"qty\":10.000,\"payAmount\":26900.00}"));
    System.err.println(string);
  }


  static class BeanWithRaw {

    private String name;
    @JsonRawValuePlus
    private String raw;

    public BeanWithRaw(String name, String raw) {
      this.name = name;
      this.raw = raw;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getRaw() {
      return raw;
    }

    public void setRaw(String raw) {
      this.raw = raw;
    }
  }
}