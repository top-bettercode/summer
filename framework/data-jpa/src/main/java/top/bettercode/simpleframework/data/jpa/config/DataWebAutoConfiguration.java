package top.bettercode.simpleframework.data.jpa.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({PageableHandlerMethodArgumentResolver.class, WebMvcConfigurer.class})
@ConditionalOnMissingBean(PageableHandlerMethodArgumentResolver.class)
@EnableConfigurationProperties(SpringDataWebProperties.class)
@AutoConfigureBefore(SpringDataWebAutoConfiguration.class)
public class DataWebAutoConfiguration {

  private final SpringDataWebProperties properties;

  public DataWebAutoConfiguration(SpringDataWebProperties properties) {
    this.properties = properties;
  }

  @Bean
  @ConditionalOnMissingBean
  public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
    return (resolver) -> {
      Pageable pageable = this.properties.getPageable();
      resolver.setPageParameterName(pageable.getPageParameter());
      resolver.setSizeParameterName(pageable.getSizeParameter());
      resolver.setOneIndexedParameters(pageable.isOneIndexedParameters());
      resolver.setPrefix(pageable.getPrefix());
      resolver.setQualifierDelimiter(pageable.getQualifierDelimiter());
      resolver.setFallbackPageable(org.springframework.data.domain.Pageable.unpaged());
      resolver.setMaxPageSize(pageable.getMaxPageSize());
    };
  }
}