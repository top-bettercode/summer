package cn.bestwu.simpleframework.data.config;

import cn.bestwu.simpleframework.data.Repositories;
import cn.bestwu.simpleframework.data.binding.WrapperBinderProperties;
import cn.bestwu.simpleframework.data.plugins.PageInfoInterceptor;
import cn.bestwu.simpleframework.data.resolver.EntityPathWrapperArgumentResolver;
import cn.bestwu.simpleframework.data.resolver.ModifyModelMethodArgumentResolver;
import cn.bestwu.simpleframework.data.resolver.PagHandlerMethodArgumentResolver;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Peter Wu
 */
@Configuration
public class MybatisPlusConfiguration {

  @ConditionalOnMissingBean(PageInfoInterceptor.class)
  @Bean
  public PageInfoInterceptor pageInfoInterceptor() {
    return new PageInfoInterceptor();
  }

  @Configuration
  @ConditionalOnWebApplication
  @EnableConfigurationProperties(WrapperBinderProperties.class)
  protected static class MybatisWebMvcConfiguration implements WebMvcConfigurer {

    private final Repositories repositories;
    private final WrapperBinderProperties properties;

    @Autowired
    public MybatisWebMvcConfiguration(Repositories repositories,
        WrapperBinderProperties properties) {
      this.repositories = repositories;
      this.properties = properties;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
      argumentResolvers.add(new PagHandlerMethodArgumentResolver());
      argumentResolvers.add(new ModifyModelMethodArgumentResolver(repositories));
      argumentResolvers.add(new EntityPathWrapperArgumentResolver(repositories, properties));
    }
  }
}
