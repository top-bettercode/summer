package top.bettercode.simpleframework.web.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import top.bettercode.simpleframework.config.JacksonExtProperties;
import top.bettercode.simpleframework.web.DataDicBean;
import top.bettercode.simpleframework.web.serializer.annotation.JsonDefault;

/**
 * @author Peter Wu
 */
public class JsonDefaultSerializerTest {

  private final JacksonExtProperties jacksonExtProperties = new JacksonExtProperties();

  @Test
  public void serialize() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMin.class);
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(new CustomNullSerializerModifier(jacksonExtProperties)));

    DataDicBean dicBean = new DataDicBean();
    assertEquals("{\"price\":0,\"path\":\"/default.jpg\",\"path1\":\"\"}",
        objectMapper.writeValueAsString(dicBean));
    dicBean.setPrice(100L);
    dicBean.setPath("/a.jpg");
    dicBean.setPath1("/b.jpg");

    assertEquals("{\"price\":100,\"path\":\"/a.jpg\",\"path1\":\"/b.jpg\"}",
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