package cn.bestwu.summer.util.wechat.config;

import com.riversoft.weixin.app.base.AppSetting;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.wechat.mini")
public class WechatMiniAppProperties extends AppSetting {

  public static final String OPEN_ID_FIELD_NAME = "openId";

}
