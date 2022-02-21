package top.bettercode.simpleframework.support;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import top.bettercode.lang.util.StringUtil;

/**
 * @author Peter Wu
 */
public class EmbeddedIdConverterTest {

  @Test
  void testToString() {
    EmbeddedIdBean embeddedIdBean = new EmbeddedIdBean();
    embeddedIdBean.setNumber1(new BigDecimal("1.234560"));
    embeddedIdBean.setName("name");
    embeddedIdBean.setIntCode(1);
    embeddedIdBean.setPrice(3L);
    embeddedIdBean.setDate(LocalDateTime.now());
    embeddedIdBean.setPaths(Lists.newArrayList("123"));
    embeddedIdBean.setPathArray(new String[]{"abc"});

    String s = EmbeddedIdConverter.toString(embeddedIdBean);
    System.err.println(StringUtil.jsonBytes(embeddedIdBean).length);
    System.err.println(s.length());
    System.err.println(s);
    EmbeddedIdBean embeddedId = EmbeddedIdConverter.toEmbeddedId(s, EmbeddedIdBean.class);
    System.err.println(embeddedId);
    Assertions.assertEquals(embeddedIdBean, embeddedId);
  }


}
