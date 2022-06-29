package top.bettercode.simpleframework.data.test.support;

import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import top.bettercode.lang.util.ArrayUtil;
import top.bettercode.simpleframework.data.jpa.config.JpaMybatisAutoConfiguration;
import top.bettercode.simpleframework.data.jpa.config.MybatisProperties;

@org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class MybatisAutoConfiguration implements InitializingBean {

  private final MybatisProperties properties;
  private final ResourceLoader resourceLoader;


  public MybatisAutoConfiguration(MybatisProperties properties,
      ResourceLoader resourceLoader) {
    this.properties = properties;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void afterPropertiesSet() {
    checkConfigFileExists();
  }

  private void checkConfigFileExists() {
    if (this.properties.isCheckConfigLocation() && StringUtils
        .hasText(this.properties.getConfigLocation())) {
      Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
      Assert.state(resource.exists(),
          "Cannot find config location: " + resource
              + " (please add config file or check your Mybatis configuration)");
    }
  }

  @Bean
  @ConditionalOnMissingBean
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
    factory.setDataSource(dataSource);
    String configLocation = this.properties.getConfigLocation();
    if (StringUtils.hasText(configLocation)) {
      factory.setConfigLocation(this.resourceLoader.getResource(configLocation));
    }
    Configuration configuration = this.properties.getConfiguration();
    factory.setConfiguration(configuration);
    factory.setConfigurationProperties(this.properties.getConfigurationProperties());
    factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
    factory.setTypeAliases(this.properties.getTypeAliases());
    factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
    factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());

    Class<TypeHandler<?>>[] typeHandlerClasses = properties.getTypeHandlerClasses();
    if (!ArrayUtil.isEmpty(typeHandlerClasses)) {
      Stream.of(typeHandlerClasses).forEach(typeHandler -> configuration.getTypeHandlerRegistry().register(typeHandler));
    }

    Resource[] mapperLocations = JpaMybatisAutoConfiguration.resolveMapperLocations(
        this.properties.getMapperLocations());
    if (!ObjectUtils.isEmpty(mapperLocations)) {
      factory.setMapperLocations(mapperLocations);
    }

    return factory.getObject();
  }

  @Bean("sqlSessionTemplate")
  @ConditionalOnMissingBean
  public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory);
  }

}
