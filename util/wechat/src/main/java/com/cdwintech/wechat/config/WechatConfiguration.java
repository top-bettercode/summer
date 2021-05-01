package com.cdwintech.wechat.config;

import com.cdwintech.wechat.controller.WechatCallbackController;
import com.cdwintech.wechat.support.DefaultDuplicatedMessageChecker;
import com.cdwintech.wechat.support.IWechatService;
import com.riversoft.weixin.mp.oauth2.MpOAuth2s;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({WechatProperties.class})
public class WechatConfiguration {

  private final Logger log = LoggerFactory.getLogger(WechatConfiguration.class);
  private final WechatProperties wechatProperties;

  public WechatConfiguration(WechatProperties wechatProperties) {
    this.wechatProperties = wechatProperties;
    try {
      String authenticationUrl = MpOAuth2s.with(wechatProperties)
          .authenticationUrl(wechatProperties.getOauthUrl(), "snsapi_userinfo");
      log.info("authenticationUrl:{}", authenticationUrl);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
//
//  @Bean
//  public WechatPushClient wechatPushClient() {
//    return new WechatPushClient(wechatProperties);
//  }

  @Bean
  public DefaultDuplicatedMessageChecker duplicatedMessageChecker() {
    return new DefaultDuplicatedMessageChecker();
  }

  @Bean
  public WechatCallbackController wechatCallbackController(IWechatService wechatService) {
    return new WechatCallbackController(wechatService, wechatProperties);
  }

}
