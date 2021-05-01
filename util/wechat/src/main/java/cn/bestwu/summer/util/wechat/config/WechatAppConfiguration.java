package cn.bestwu.summer.util.wechat.config;

import cn.bestwu.summer.util.wechat.controller.WechatAppCallbackController;
import cn.bestwu.summer.util.wechat.support.IWechatService;
import com.riversoft.weixin.app.base.AppSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(AppSetting.class)
@ConditionalOnProperty(prefix = "wechat.mini", name = "appId")
@Configuration
@EnableConfigurationProperties({WechatMiniAppProperties.class})
public class WechatAppConfiguration {

  private final Logger log = LoggerFactory.getLogger(WechatAppConfiguration.class);
  private final WechatMiniAppProperties wechatMiniAppProperties;

  public WechatAppConfiguration(WechatMiniAppProperties wechatMiniAppProperties) {
    this.wechatMiniAppProperties = wechatMiniAppProperties;
  }


  @Bean
  public WechatAppCallbackController wechatAppCallbackController(IWechatService wechatService) {
    return new WechatAppCallbackController(wechatService, wechatMiniAppProperties);
  }


}
