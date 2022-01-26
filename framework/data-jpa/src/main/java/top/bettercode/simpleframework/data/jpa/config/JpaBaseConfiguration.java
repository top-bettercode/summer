package top.bettercode.simpleframework.data.jpa.config;

import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import java.util.LinkedHashMap;
import javax.sql.DataSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author Peter Wu
 */
public class JpaBaseConfiguration {

  public static HikariDataSource getDataSource(DataSourceProperties dataSourceProperties,
      Environment environment, String poolKey) {
    HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
        .type(HikariDataSource.class).build();
    if (StringUtils.hasText(dataSourceProperties.getName())) {
      dataSource.setPoolName(dataSourceProperties.getName());
    }

    Binder.get(environment).bind(poolKey, Bindable.ofInstance(dataSource));
    return dataSource;
  }

  public static LocalContainerEntityManagerFactoryBean getEntityManagerFactory(
      EntityManagerFactoryBuilder builder,
      DataSource dataSource,
      HibernateProperties hibernateProperties, JpaProperties jpaProperties, String[] basePackages) {

    return builder
        .dataSource(dataSource)
        .properties(new LinkedHashMap<>(hibernateProperties
            .determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings()
            )))
        .packages(basePackages)
        .build();
  }

  public static PlatformTransactionManager getTransactionManager(
      LocalContainerEntityManagerFactoryBean crmEntityManagerFactory) {
    return new JpaTransactionManager(crmEntityManagerFactory.getObject());
  }

  public static SqlSessionFactory getSqlSessionFactory(DataSource dataSource,
      MybatisProperties properties,
      ResourceLoader resourceLoader, PageHelperProperties pageHelperProperties) throws Exception {
    SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
    factory.setDataSource(dataSource);
    if (StringUtils.hasText(properties.getConfigLocation())) {
      factory
          .setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
    }
    Configuration configuration = new Configuration();
    Configuration propertiesConfiguration = properties.getConfiguration();
    if (propertiesConfiguration != null) {
      BeanUtils.copyProperties(propertiesConfiguration, configuration);
    }
    PageInterceptor interceptor = new PageInterceptor();
    interceptor.setProperties(pageHelperProperties.getProperties());
    configuration.addInterceptor(interceptor);
    factory.setConfiguration(configuration);
    if (properties.getConfigurationProperties() != null) {
      factory.setConfigurationProperties(properties.getConfigurationProperties());
    }

    if (StringUtils.hasLength(properties.getTypeAliasesPackage())) {
      factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
    }
    if (properties.getTypeAliasesSuperType() != null) {
      factory.setTypeAliasesSuperType(properties.getTypeAliasesSuperType());
    }
    if (StringUtils.hasLength(properties.getTypeHandlersPackage())) {
      factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
    }

    if (!ObjectUtils.isEmpty(properties.resolveMapperLocations())) {
      factory.setMapperLocations(properties.resolveMapperLocations());
    }
    return factory.getObject();
  }


  public static SqlSessionTemplate getSqlSessionTemplate(SqlSessionFactory sqlSessionFactory,
      MybatisProperties properties) {
    ExecutorType executorType = properties.getExecutorType();
    return executorType != null ? new SqlSessionTemplate(sqlSessionFactory, executorType)
        : new SqlSessionTemplate(sqlSessionFactory);
  }


}
