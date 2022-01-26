package top.bettercode.simpleframework.data.jpa.config;

import com.github.pagehelper.PageInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.EnableJpaExtRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 *
 */
public class RepositoryBeanDefinitionRegistryPostProcessor implements
    BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, EnvironmentAware {

  private static final Logger logger = LoggerFactory.getLogger(
      RepositoryBeanDefinitionRegistryPostProcessor.class);

  private ResourceLoader resourceLoader;
  private Environment environment;


  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    Map<String, BaseDataSourceProperties> dataSources = Binder.get(
        environment).bind("spring.datasources", Bindable
        .mapOf(String.class, BaseDataSourceProperties.class)).orElse(null);
    if (dataSources != null) {
      DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
      for (Entry<String, BaseDataSourceProperties> entry : dataSources.entrySet()) {
        //dataSource
        BaseDataSourceProperties properties = entry.getValue();
        boolean primary = "primary".equals(entry.getKey());
        EnableJpaExtRepositories jpaExtRepositories = properties.getEnableJpaExtRepositoriesAnnotationConfiguration()
            .getAnnotation(EnableJpaExtRepositories.class);
        String dataSourceBeanName = primary ? "dataSource" : entry.getKey() + "DataSource";
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            HikariDataSource.class, () -> {
              DataSourceProperties dataSourceProperties = new DataSourceProperties();
              dataSourceProperties.setUrl(properties.getUrl());
              dataSourceProperties.setUsername(properties.getUsername());
              dataSourceProperties.setPassword(properties.getPassword());

              HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                  .type(HikariDataSource.class).build();
              if (StringUtils.hasText(dataSourceProperties.getName())) {
                dataSource.setPoolName(dataSourceProperties.getName());
              }

              Binder.get(environment)
                  .bind("spring.datasource.hikari", Bindable.ofInstance(dataSource));
              return dataSource;
            });
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(dataSourceBeanName);
        }
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(dataSourceBeanName, beanDefinition);

        //entityManagerFactory
        String entityManagerFactoryBeanName = jpaExtRepositories.entityManagerFactoryRef();
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            LocalContainerEntityManagerFactoryBean.class, () -> {

              EntityManagerFactoryBuilder builder = beanFactory.getBean(
                  EntityManagerFactoryBuilder.class);
              DataSource dataSource = beanFactory.getBean(dataSourceBeanName,
                  DataSource.class);
              HibernateProperties hibernateProperties = beanFactory.getBean(
                  HibernateProperties.class);
              JpaProperties jpaProperties = beanFactory.getBean(JpaProperties.class);

              return builder
                  .dataSource(dataSource)
                  .properties(new LinkedHashMap<>(hibernateProperties
                      .determineHibernateProperties(jpaProperties.getProperties(),
                          new HibernateSettings()
                      )))
                  .packages(jpaExtRepositories.basePackages())
                  .build();
            });
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(entityManagerFactoryBeanName);
        }
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(entityManagerFactoryBeanName, beanDefinition);

        //transactionManager
        String transactionManagerBeanName = jpaExtRepositories.transactionManagerRef();
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            PlatformTransactionManager.class, () -> {
              EntityManagerFactory entityManagerFactory = beanFactory.getBean(
                  entityManagerFactoryBeanName,
                  EntityManagerFactory.class);
              return new JpaTransactionManager(entityManagerFactory);
            });
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(transactionManagerBeanName);
        }
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(transactionManagerBeanName, beanDefinition);

        //sqlSessionFactory
        String sqlSessionFactoryBeanName =
            primary ? "sqlSessionFactory" : entry.getKey() + "SqlSessionFactory";
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            SqlSessionFactory.class, () ->
                getSqlSessionFactory(
                    beanFactory.getBean(dataSourceBeanName, DataSource.class),
                    beanFactory.getBean(MybatisProperties.class),
                    resourceLoader,
                    beanFactory.getBean(PageHelperProperties.class)
                ));
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(sqlSessionFactoryBeanName);
        }
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(sqlSessionFactoryBeanName, beanDefinition);

        // sqlSessionTemplate
        String sqlSessionTemplateBeanName = jpaExtRepositories.sqlSessionTemplateRef();
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            SqlSessionTemplate.class, () -> {
              SqlSessionFactory sqlSessionFactory = beanFactory.getBean(sqlSessionFactoryBeanName,
                  SqlSessionFactory.class);
              ExecutorType executorType = beanFactory.getBean(MybatisProperties.class)
                  .getExecutorType();
              return executorType != null ? new SqlSessionTemplate(sqlSessionFactory, executorType)
                  : new SqlSessionTemplate(sqlSessionFactory);
            }
        );
        if (primary) {
          beanDefinitionBuilder.setPrimary(primary);
          factory.removeBeanDefinition(sqlSessionTemplateBeanName);
        }
        beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setSynthetic(true);
        beanDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
        factory.registerBeanDefinition(sqlSessionTemplateBeanName, beanDefinition);
      }
    }
  }

  private SqlSessionFactory getSqlSessionFactory(DataSource dataSource,
      MybatisProperties properties,
      ResourceLoader resourceLoader, PageHelperProperties pageHelperProperties) {
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
    try {
      return factory.getObject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {

  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}