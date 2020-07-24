package cn.bestwu.simpleframework.support.setting;

import cn.bestwu.lang.util.StringUtil;
import cn.bestwu.simpleframework.config.CorsProperties;
import java.util.Properties;
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
    CorsProperties corsProperties = setting.bind("security.cors", CorsProperties.class);
    System.err.println(StringUtil.valueOf(corsProperties, true));

    CorsProperties corsProperties2=new CorsProperties();
    corsProperties2.setEnable(false);
    System.err.println(StringUtil.valueOf(corsProperties2,true));
    BeanUtils.copyProperties(corsProperties2,corsProperties);
//    corsProperties.setEnable(false);
//    setting.put("security.cors.enable", "false");
    System.err.println(StringUtil.valueOf(corsProperties, true));

    System.err.println(map);

  }
}