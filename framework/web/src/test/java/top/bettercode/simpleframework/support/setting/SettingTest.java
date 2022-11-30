package top.bettercode.simpleframework.support.setting;

import java.util.HashMap;
import java.util.Map;
import kotlin.collections.CollectionsKt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import top.bettercode.lang.util.StringUtil;
import top.bettercode.simpleframework.config.CorsProperties;

/**
 * @author Peter Wu
 */
class SettingTest {

  String key = "summer.security.cors.enable";
  @Test
  void getAndset() {
    Map<String, String> map = new HashMap<>();
    map.put(key, "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    CorsProperties corsProperties2 = new CorsProperties();
    corsProperties2.setEnable(false);
    System.err.println(StringUtil.valueOf(corsProperties2, true));
    BeanUtils.copyProperties(corsProperties2, corsProperties);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals("false", map.get(key));
    Assertions.assertEquals("false", setting.get(key));
  }

  @Test
  void getAndset1() {
    Map<String, String> map = new HashMap<>();
    map.put(key, "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    corsProperties.setAllowedOrigins(CollectionsKt.listOf("*", "orign"));
    corsProperties.setEnable(false);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals("false", map.get(key));
    Assertions.assertEquals("false", setting.get(key));
  }

  @Test
  void getAndset2() {
    Map<String, String> map = new HashMap<>();
    map.put(key, "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    setting.put(key, "false");
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals("false", map.get(key));
    Assertions.assertEquals("false", setting.get(key));
  }

  @Test
  void getAndset3() {
    Map<String, String> map = new HashMap<>();
    map.put(key, "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    map.put(key, "false");
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals("false", map.get(key));
    Assertions.assertEquals("false", setting.get(key));
  }

  @Test
  void getAndset4() {
    Map<String, String> map = new HashMap<>();
    map.put(key, "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    setting.put(key, "false");
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals("false", map.get(key));
    Assertions.assertEquals("false", setting.get(key));
  }

  @Test
  void getAndset5() {
    Map<String, String> map = new HashMap<>();
    map.put(key, "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    setting.put(key, "false");
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals("false", map.get(key));
    Assertions.assertEquals("false", setting.get(key));
  }

}