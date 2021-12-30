package top.bettercode.summer.util.wechat.config;

import com.riversoft.weixin.app.base.AppSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.bettercode.summer.util.wechat.controller.WechatAppCallbackController;
import top.bettercode.summer.util.wechat.support.IWechatService;

@ConditionalOnClass(AppSetting.class)
@ConditionalOnProperty(prefix = "summer.wechat.mini", name = "app-id")
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
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
