package top.bettercode.simpleframework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Peter Wu
 */
@ConditionalOnProperty(name = "summer.async.enabled", havingValue = "true")
@EnableAsync(proxyTargetClass = true)
@Configuration(proxyBeanMethods = false)
public class AsyncConfiguration {

  public AsyncConfiguration() {
    Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);
    log.info("------------启用 Spring 的异步方法执行功能------------");
  }
}
