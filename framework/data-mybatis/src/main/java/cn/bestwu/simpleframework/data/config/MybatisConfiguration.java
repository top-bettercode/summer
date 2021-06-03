package cn.bestwu.simpleframework.data.config;

import cn.bestwu.simpleframework.data.IbatisErrorHandler;
import cn.bestwu.simpleframework.data.Repositories;
import cn.bestwu.simpleframework.data.dsl.EntityPathWrapper;
import cn.bestwu.simpleframework.support.packagescan.PackageScanClassResolver;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Peter Wu
 */
@Configuration(proxyBeanMethods = false)
public class MybatisConfiguration {

  /**
   * specifying the packages to scan for mixIn annotation.
   */
  @Value("${summer.data.mybatis.binding.query-dsl.base-packages:}")
  private String[] basePackages;

  private final List<MapperFactoryBean> mapperFactoryBeans;

  public MybatisConfiguration(
      @Autowired(required = false) List<MapperFactoryBean> mapperFactoryBeans) {
    this.mapperFactoryBeans = mapperFactoryBeans;
  }

  @Bean
  public IbatisErrorHandler ibatisErrorHandler(MessageSource messageSource,
      HttpServletRequest request) {
    return new IbatisErrorHandler(messageSource, request);
  }

  @Bean
  public Repositories repositories(ApplicationContext applicationContext,
      PackageScanClassResolver packageScanClassResolver) {
    Set<String> packages = PackageScanClassResolver
        .detectPackagesToScan(applicationContext, basePackages);
    Set<Class<?>> allSubClasses = packageScanClassResolver
        .findImplementations(EntityPathWrapper.class, packages.toArray(new String[0]));
    return new Repositories(mapperFactoryBeans, allSubClasses);
  }

}
