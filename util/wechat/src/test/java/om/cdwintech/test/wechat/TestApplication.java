package om.cdwintech.test.wechat;

import cn.bestwu.summer.util.wechat.config.WechatProperties;
import cn.bestwu.summer.util.wechat.support.AbstractWechatService;
import cn.bestwu.summer.util.wechat.support.DuplicatedMessageChecker;
import cn.bestwu.summer.util.wechat.support.IWechatService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

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
