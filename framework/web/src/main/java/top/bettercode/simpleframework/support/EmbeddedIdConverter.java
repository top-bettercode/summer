package top.bettercode.simpleframework.support;

import java.nio.charset.StandardCharsets;
import org.springframework.util.Base64Utils;
import top.bettercode.lang.util.StringUtil;

/**
 * @author Peter Wu
 */
public class EmbeddedIdConverter {

  public static <T> String toString(T embeddedId) {
    byte[] bytes = StringUtil.jsonBytes(embeddedId);
    bytes = Base64Utils.encodeUrlSafe(bytes);
    return new String(bytes);
  }

  public static <T> T toEmbeddedId(String src, Class<T> type) {
    byte[] bytes = src.getBytes(StandardCharsets.UTF_8);
    bytes = Base64Utils.decodeUrlSafe(bytes);
    return StringUtil.readJson(bytes, type);
  }


}
