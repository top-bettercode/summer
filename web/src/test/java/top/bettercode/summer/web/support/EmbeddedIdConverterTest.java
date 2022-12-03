package top.bettercode.summer.web.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import top.bettercode.summer.tools.lang.util.StringUtil;

/**
 * @author Peter Wu
 */
public class EmbeddedIdConverterTest {

  @Test
  void testToString() {
    EmbeddedIdBean embeddedIdBean = new EmbeddedIdBean();
    embeddedIdBean.setName("name");
    embeddedIdBean.setIntCode(1);
    embeddedIdBean.setPrice(3L);

    String s = EmbeddedIdConverter.toString(embeddedIdBean);
    System.err.println(StringUtil.jsonBytes(embeddedIdBean).length);
    System.err.println(s.length());
    System.err.println(s);
    EmbeddedIdBean embeddedId = EmbeddedIdConverter.toEmbeddedId(s, EmbeddedIdBean.class);
    System.err.println(embeddedId);
    Assertions.assertEquals(embeddedIdBean, embeddedId);
  }

}
