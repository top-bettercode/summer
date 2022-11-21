package top.bettercode.simpleframework.web.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import top.bettercode.simpleframework.config.JacksonExtProperties;
import top.bettercode.simpleframework.web.DataDicBean;
import top.bettercode.simpleframework.web.serializer.annotation.JsonDefault;

/**
 * @author Peter Wu
 */
public class JsonDefaultSerializerTest {

  private final JacksonExtProperties jacksonExtProperties = new JacksonExtProperties();
  final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(new CustomNullSerializerModifier(jacksonExtProperties)));

    MockEnvironment mockEnvironment = new MockEnvironment();
//    mockEnvironment.setProperty("summer.multipart.file-url-format", "/path%s");
    mockEnvironment.setProperty("summer.multipart.file-url-format", "http://127.0.0.1%s");
    mockEnvironment.setProperty("path1-url", "http://127.0.0.2%s");
    UrlSerializer.setEnvironment(mockEnvironment);
  }

  @Test
  public void serialize() throws JsonProcessingException {
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMin.class);
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(new CustomNullSerializerModifier(jacksonExtProperties)));

    DataDicBean dicBean = new DataDicBean();
    assertEquals(
        "{\"number1\":0,\"number22\":null,\"price\":0,\"path\":\"/default.jpg\",\"pathUrl\":\"http://127.0.0.1/default.jpg\",\"path1\":\"\"}",
        objectMapper.writeValueAsString(dicBean));

    dicBean.setNumber1(new BigDecimal(100));
    dicBean.setPath("/a.jpg");
    dicBean.setPath1("/b.jpg");

    assertEquals(
        "{\"number1\":100,\"number22\":null,\"price\":0,\"path\":\"/a.jpg\",\"pathUrl\":\"http://127.0.0.1/a.jpg\",\"path1\":\"/b.jpg\"}",
        objectMapper.writeValueAsString(dicBean));

    dicBean = new DataDicBean();
    dicBean.setNumber2(new BigDecimal(10));
    assertEquals(
        "{\"number1\":0,\"number2\":10.000,\"number22\":10,\"price\":0,\"path\":\"/default.jpg\",\"pathUrl\":\"http://127.0.0.1/default.jpg\",\"path1\":\"\"}",
        objectMapper.writeValueAsString(dicBean));

  }

  @Test
  public void defSerialize() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMin.class);
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(new CustomNullSerializerModifier(jacksonExtProperties)));

    DataDicBean dicBean = new DataDicBean();
    assertEquals(
        "{\"price\":0,\"path\":\"/default.jpg\",\"path1\":\"\"}",
        objectMapper.writeValueAsString(dicBean));
    dicBean.setPrice(100L);
    dicBean.setPath("/a.jpg");
    dicBean.setPath1("/b.jpg");

    assertEquals("{\"price\":100,\"path\":\"/a.jpg\",\"path1\":\"/b.jpg\"}",
        objectMapper.writeValueAsString(dicBean));

  }

  interface DataDicBeanMin {

    @JsonDefault
    String getPath1();

    @JsonDefault("/default.jpg")
    String getPath();

    @JsonDefault("0")
    Long getPrice();

  }
}