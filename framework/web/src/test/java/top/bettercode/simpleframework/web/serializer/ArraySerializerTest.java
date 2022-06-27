package top.bettercode.simpleframework.web.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.bettercode.simpleframework.web.StringArrayBean;

/**
 * @author Peter Wu
 */
class ArraySerializerTest {

  final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
  }

  @Test
  public void serialize() throws Exception {
    StringArrayBean bean = new StringArrayBean();
    bean.setAry("a,b,c,");
    String actual = objectMapper.writeValueAsString(bean);
    System.err.println(actual);
    assertEquals("{\"ary\":\"a,b,c,\",\"aryArray\":[\"a\",\"b\",\"c\"]}", actual);
  }

  @Test
  public void serialize1() throws Exception {
    StringArrayBean bean = new StringArrayBean();
    bean.setAry("a,b,c");
    String actual = objectMapper.writeValueAsString(bean);
    System.err.println(actual);
    assertEquals("{\"ary\":\"a,b,c\",\"aryArray\":[\"a\",\"b\",\"c\"]}", actual);
  }

}