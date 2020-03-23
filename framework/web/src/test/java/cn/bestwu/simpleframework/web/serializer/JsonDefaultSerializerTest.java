package cn.bestwu.simpleframework.web.serializer;

import cn.bestwu.simpleframework.web.DataDicBean;
import cn.bestwu.simpleframework.web.serializer.annotation.JsonDefault;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Peter Wu
 */
public class JsonDefaultSerializerTest {

  @Test
  public void serialize() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMin.class);
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(new CustomNullSerializerModifier(true)));

    DataDicBean dicBean = new DataDicBean();
    Assert.assertEquals("{\"price\":0,\"path\":\"/default.jpg\",\"path1\":\"\"}",
        objectMapper.writeValueAsString(dicBean));
    dicBean.setPrice(100L);
    dicBean.setPath("/a.jpg");
    dicBean.setPath1("/b.jpg");
    Assert
        .assertEquals("{\"price\":100,\"path\":\"/a.jpg\",\"path1\":\"/b.jpg\"}",
            objectMapper.writeValueAsString(dicBean));

  }

  @Test
  public void defSerialize() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.addMixIn(DataDicBean.class, DataDicBeanMin.class);
    objectMapper.setDefaultPropertyInclusion(Include.NON_NULL);
    objectMapper.setSerializerFactory(objectMapper.getSerializerFactory()
        .withSerializerModifier(new CustomNullSerializerModifier(true)));

    DataDicBean dicBean = new DataDicBean();
    Assert.assertEquals(
        "{\"price\":0,\"path\":\"/default.jpg\",\"path1\":\"\"}",
        objectMapper.writeValueAsString(dicBean));
    dicBean.setPrice(100L);
    dicBean.setPath("/a.jpg");
    dicBean.setPath1("/b.jpg");
    Assert
        .assertEquals("{\"price\":100,\"path\":\"/a.jpg\",\"path1\":\"/b.jpg\"}",
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