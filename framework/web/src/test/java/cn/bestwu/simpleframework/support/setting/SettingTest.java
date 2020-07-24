package cn.bestwu.simpleframework.support.setting;

import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.simpleframework.config.CorsProperties;
import java.util.Properties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

/**
 * @author Peter Wu
 */
class SettingTest {

  @Test
  void getAndset() {
    Properties map = new Properties();
    map.put("security.cors.enable", "true");
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
    Assertions.assertEquals(false, map.get("security.cors.enable"));
    Assertions.assertEquals(false, setting.get("security.cors.enable"));
  }

  @Test
  void getAndset1() {
    Properties map = new Properties();
    map.put("security.cors.enable", "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    corsProperties.setEnable(false);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals(false, map.get("security.cors.enable"));
    Assertions.assertEquals(false, setting.get("security.cors.enable"));
  }

  @Test
  void getAndset2() {
    Properties map = new Properties();
    map.put("security.cors.enable", "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    setting.put("security.cors.enable", "false");
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals("false", map.get("security.cors.enable"));
    Assertions.assertEquals("false", setting.get("security.cors.enable"));
  }

  @Test
  void getAndset3() {
    Properties map = new Properties();
    map.put("security.cors.enable", "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    map.put("security.cors.enable", "false");
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals("false", map.get("security.cors.enable"));
    Assertions.assertEquals("false", setting.get("security.cors.enable"));
  }

  @Test
  void getAndset4() {
    Properties map = new Properties();
    map.put("security.cors.enable", "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    setting.put("security.cors.enable", false);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals(false, map.get("security.cors.enable"));
    Assertions.assertEquals(false, setting.get("security.cors.enable"));
  }

  @Test
  void getAndset5() {
    Properties map = new Properties();
    map.put("security.cors.enable", "true");
    Setting setting = Setting.of(map);
    CorsProperties corsProperties = setting.bind(CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    map.put("security.cors.enable", false);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);
    Assertions.assertFalse(corsProperties.isEnable());
    Assertions.assertEquals(false, map.get("security.cors.enable"));
    Assertions.assertEquals(false, setting.get("security.cors.enable"));
  }

}