package om.cdwintech.test.wechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import top.bettercode.summer.util.wechat.config.WechatProperties;
import top.bettercode.summer.util.wechat.support.AbstractWechatService;
import top.bettercode.summer.util.wechat.support.DuplicatedMessageChecker;
import top.bettercode.summer.util.wechat.support.IWechatService;

/**
 * @author Peter Wu
 */
@SpringBootApplication
public class TestApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

  @Bean
  public IWechatService wechatService(DuplicatedMessageChecker duplicatedMessageChecker,
      WechatProperties wechatProperties) {
    return new AbstractWechatService() {
      @Override
      public String oauth(String openId) {
        return null;
      }
    };
  }
}
